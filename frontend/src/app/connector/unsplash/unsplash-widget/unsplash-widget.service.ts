import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {WallpaperService} from '../../../services/wallpaper/wallpaper.service';
import {BehaviorSubject} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class UnsplashWidgetService {
    constructor(
        private httpClient: HttpClient,
        private wallpaperService: WallpaperService,
    ) {
        this.setCurrentWallpaper();
    }

    isFirstSubject = new BehaviorSubject<boolean>(this.getCounter() === 0);
    isFirst$ = this.isFirstSubject.asObservable();
    isLastSubject = new BehaviorSubject<boolean>(this.getCounter() === this.getPreviousWallpapers().length);
    isLast$ = this.isLastSubject.asObservable();

    setCurrentWallpaper() {
        const currentWallpaper = this.getCurrentWallpaper();

        if (currentWallpaper) {
            this.wallpaperService.setWallpaper(currentWallpaper);
        } else {
            this.getNewWallpaper();
            this.wallpaperService.setWallpaper(this.getCurrentWallpaper()!)
        }

        this.setCounter(this.getPreviousWallpapers().length);
    }

    setPreviousWallpaper() {
        let counter = this.getCounter();
        if (counter && counter > 0) {

            counter--
            const previousWallpaper = this.getPreviousWallpapers().at(counter);
            if (previousWallpaper) {

                this.wallpaperService.setWallpaper(previousWallpaper);
            }
            this.setCounter(counter);

            this.isLastSubject.next(false);
            this.isFirstSubject.next(counter === 0);
        }
    }

    setNextWallpaper() {
        let counter = this.getCounter();
        const wallpapersCount = this.getPreviousWallpapers().length;
        if (counter && counter + 1 < wallpapersCount) {
            this.isFirstSubject.next(false);

            const previousWallpaper = this.getPreviousWallpapers().at(counter + 1);

            if (previousWallpaper) {
                this.wallpaperService.setWallpaper(previousWallpaper);
                counter++
                this.setCounter(counter);
            }
        } else if (counter && counter + 1 === wallpapersCount) {
            this.isFirstSubject.next(false);

            this.getNewWallpaper();
            this.setCounter(this.getPreviousWallpapers().length);
            this.isLastSubject.next(true);
        }
    }


    private getNewWallpaper() {
        this.httpClient.get<any>(`${environment.baseUrl}/unsplash/photo?topic=wallpapers`).subscribe(result => {
            const wallpaper = result.urls.full;

            this.addNewWallpaper(wallpaper);
        })
    }

    private addNewWallpaper(wallpaper: string) {
        const previousWallpapers = this.getPreviousWallpapers() ?? [];

        if (previousWallpapers.length >= 10) {
            previousWallpapers.shift();
        }

        previousWallpapers.push(wallpaper);

        this.setPreviousWallpapers(previousWallpapers);
    }

    private getCurrentWallpaper(): string | null {
        const currentWallpaper = this.getPreviousWallpapers().at(-1)

        return currentWallpaper ?? null;
    }

    private getCounter(): number {
        const savedNumber = localStorage.getItem('wallpaper-counter');

        return savedNumber ? Number(savedNumber) : -1;
    }

    private setCounter(counter: number) {
        localStorage.setItem('wallpaper-counter', counter.toString());
    }

    private getPreviousWallpapers(): string[] {
        const item = localStorage.getItem('previous-wallpapers');

        return item ? JSON.parse(item) : [];
    }

    private setPreviousWallpapers(wallpapers: string[]) {
        localStorage.setItem('previous-wallpapers', JSON.stringify(wallpapers));
    }

}
