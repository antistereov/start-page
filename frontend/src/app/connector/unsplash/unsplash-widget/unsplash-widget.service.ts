import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {WallpaperService} from '../../../components/shared/wallpaper/wallpaper.service';
import {BehaviorSubject, map, Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class UnsplashWidgetService {
    constructor(
        private httpClient: HttpClient,
        private wallpaperService: WallpaperService,
    ) {
        this.setCurrentWallpaper();
        console.log(this.getCounter(), this.getPreviousWallpapers().length)
        // console.log(this.getCounter() + 1 === this.getPreviousWallpapers().length)
    }

    isFirstSubject = new BehaviorSubject<boolean>(this.getCounter() === 0);
    isFirst$ = this.isFirstSubject.asObservable();
    isLastSubject = new BehaviorSubject<boolean>(this.getCounter() + 1 === this.getPreviousWallpapers().length);
    isLast$ = this.isLastSubject.asObservable();

    setCurrentWallpaper() {
        const currentWallpaper = this.getCurrentWallpaper();

        if (currentWallpaper) {
            this.wallpaperService.setWallpaper(currentWallpaper);
        } else {
            this.getNewWallpaper();
            this.wallpaperService.setWallpaper(this.getCurrentWallpaper()!)
        }

        this.setCounter(this.getPreviousWallpapers().length - 1);
    }

    setPreviousWallpaper() {
        let counter = this.getCounter();
        if (counter > 0) {

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
        if (counter + 1 < wallpapersCount) {
            console.log("next saved")
            this.isFirstSubject.next(false);

            counter++
            const previousWallpaper = this.getPreviousWallpapers().at(counter);

            if (previousWallpaper) {
                this.wallpaperService.setWallpaper(previousWallpaper);
                this.setCounter(counter);

                if (counter + 1 === wallpapersCount) {
                    this.isLastSubject.next(true);
                }
            }
        } else if (counter + 1 === wallpapersCount) {
            this.isFirstSubject.next(false);
            this.isLastSubject.next(true);

            this.getNewWallpaper().subscribe(result => {
                if (result) {
                    this.wallpaperService.setWallpaper(result);
                    this.setCounter(this.getPreviousWallpapers().length - 1)
                }
            });
        }
    }

    private getNewWallpaper(): Observable<string | null> {
        return this.httpClient.get<any>(`${environment.baseUrl}/unsplash/photo?topic=wallpapers`).pipe(
            map(result => {
                const wallpaper = result.urls.full;

                if (wallpaper) {
                    this.addNewWallpaper(wallpaper);
                    return wallpaper;
                } else {
                    return null;
                }
            })
        );
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
        const currentWallpaper = this.getPreviousWallpapers().at(-1);

        return currentWallpaper ?? null;
    }

    private getCounter(): number {
        const savedNumber = localStorage.getItem('wallpaper-counter');

        return savedNumber ? Number(savedNumber) : 0;
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
