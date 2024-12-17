import { Injectable } from '@angular/core';
import {WallpaperService} from '../../../components/shared/wallpaper/wallpaper.service';
import {BehaviorSubject, map} from 'rxjs';
import {UnsplashPhotoService} from '../unsplash-photo/unsplash-photo.service';
import {RandomParams} from 'unsplash-js/dist/methods/photos';
import {db, UnsplashWallpaper} from './unsplash-wallpaper-db.service';
import {UnsplashPhoto} from '../unsplash-photo/unsplash-photo.model';

@Injectable({
    providedIn: 'root'
})
export class UnsplashWallpaperService {
    private isFirstSubject = new BehaviorSubject<boolean>(false);
    isFirst$ = this.isFirstSubject.asObservable();
    private isLastSubject = new BehaviorSubject<boolean>(false);
    isLast$ = this.isLastSubject.asObservable();

    private currentWallpaperSubject = new BehaviorSubject<UnsplashPhoto | undefined>(undefined);
    currentWallpaper$ = this.currentWallpaperSubject.asObservable();

    public randomParams: RandomParams = {
        orientation: 'landscape',
        contentFilter: 'low',
    }

    constructor(
        private unsplashPhotoService: UnsplashPhotoService,
        private wallpaperService: WallpaperService,
    ) {
        this.setCurrentWallpaper().then()
    }

    async setCurrentWallpaper() {
        const currentWallpaper = await this.getCurrentWallpaper();


        if (currentWallpaper) {
            const counter = currentWallpaper.id
            const firstCounter = await this.getFirstCounter();
            const lastCounter = await this.getLastCounter();

            this.unsplashPhotoService.getPhoto(currentWallpaper.photoId).subscribe(photo => {

                this.wallpaperService.setWallpaper(photo.urls.full);

                this.isFirstSubject.next(counter === firstCounter);
                this.isLastSubject.next(counter === lastCounter);

                this.currentWallpaperSubject.next(photo);
            })

        } else {
            this.getNewWallpaper().subscribe();
        }
    }

    async setPreviousWallpaper() {
        let counter = this.getCounter();

        if (counter > 0) {
            const previousWallpaper = await db.wallpaperIds.get(counter - 1);
            const firstCounter = await this.getFirstCounter()

            if (firstCounter) {
                if (previousWallpaper) {
                    this.unsplashPhotoService.getPhoto(previousWallpaper.photoId).subscribe(photo => {
                        this.wallpaperService.setWallpaper(photo.urls.full);

                        this.setCounter(counter - 1);

                        this.isFirstSubject.next(counter - 1 === firstCounter);
                        this.isLastSubject.next(false);
                        this.currentWallpaperSubject.next(photo);
                    })
                }
            } else {
                console.error("Cannot access first Unsplash wallpaper from IndexedDB")
            }
        }
    }

    async setNextWallpaper() {
        let counter = this.getCounter();
        const lastCounter = await this.getLastCounter();

        if (lastCounter && counter < lastCounter) {
            const nextWallpaper = await db.wallpaperIds.get(counter + 1);

            if (nextWallpaper) {
                this.unsplashPhotoService.getPhoto(nextWallpaper.photoId).subscribe(photo => {
                    this.wallpaperService.setWallpaper(photo.urls.full);
                    this.setCounter(counter + 1);

                    this.isFirstSubject.next(false)
                    this.isLastSubject.next(counter + 1 === lastCounter);
                    this.currentWallpaperSubject.next(photo);
                });
            }
        } else {
            this.getNewWallpaper().subscribe();
        }
    }

    toggleLike() {
        const currentWallpaper = this.currentWallpaperSubject.value

        if (currentWallpaper) {
            if (currentWallpaper.likedByUser) {
                this.unsplashPhotoService.unlikePhoto(currentWallpaper.id).subscribe(() => {
                    const updatedWallpaper = { ...currentWallpaper, likedByUser: false }
                    this.currentWallpaperSubject.next(updatedWallpaper);
                });
            } else {
                this.unsplashPhotoService.likePhoto(currentWallpaper.id).subscribe(() => {
                    const updatedWallpaper = { ...currentWallpaper, likedByUser: true }
                    this.currentWallpaperSubject.next(updatedWallpaper);
                });
            }
        }
    }

    private getNewWallpaper() {
        return this.unsplashPhotoService.getRandomPhoto(this.randomParams).pipe(
            map((photo) => {
                const wallpaper: UnsplashWallpaper = { dateAdded: new Date(), photoId: photo.id };

                this.addNewWallpaper(wallpaper).then(() => {
                    this.wallpaperService.setWallpaper(photo.urls.full);

                    this.currentWallpaperSubject.next(photo);

                    this.getLastCounter().then(lastCounter => {
                        if (lastCounter) {
                            this.setCounter(lastCounter);

                            this.getFirstCounter().then(firstCounter => {
                                this.isFirstSubject.next(lastCounter === firstCounter);
                            });
                            this.isLastSubject.next(true);
                        }
                    });
                });
            })
        )
    }

    private async addNewWallpaper(wallpaper: UnsplashWallpaper) {
        await db.wallpaperIds.add(wallpaper);

        const wallpapersCount = await db.wallpaperIds.count();

        if (wallpapersCount > 20) {
            const excessCount = wallpapersCount - 20;

            const oldWallpapers = await db.wallpaperIds
                .orderBy('dateAdded')
                .limit(excessCount)
                .toArray()

            for (const oldWallpaper of oldWallpapers) {
                if (oldWallpaper.id) {
                    await db.wallpaperIds.delete(oldWallpaper.id);
                }
            }
        }

    }

    private getLastWallpaper(): Promise<UnsplashWallpaper | undefined> {
        return db.wallpaperIds.orderBy('dateAdded').last();
    }

    private getCurrentWallpaper(): Promise<UnsplashWallpaper | undefined> {
        const currenCounter = this.getCounter();
        return db.wallpaperIds.get(currenCounter);
    }

    private async getLastCounter(): Promise<number | undefined> {
        const lastWallpaper = await this.getLastWallpaper();

        return lastWallpaper?.id;
    }

    private getFirstWallpaper(): Promise<UnsplashWallpaper | undefined> {
        return db.wallpaperIds.orderBy('dateAdded').first();
    }

    private async getFirstCounter(): Promise<number | undefined> {
        const firstWallpaper = await this.getFirstWallpaper();

        return firstWallpaper?.id;
    }

    private getCounter(): number {
        const savedNumber = localStorage.getItem('wallpaper-counter');

        return savedNumber ? Number(savedNumber) : 0;
    }

    private setCounter(counter: number) {
        localStorage.setItem('wallpaper-counter', counter.toString());
    }
}
