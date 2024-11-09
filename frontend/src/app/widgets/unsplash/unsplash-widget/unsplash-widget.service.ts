import { Injectable } from '@angular/core';
import {BehaviorSubject, Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {WallpaperService} from '../../../theme/wallpaper/wallpaper.service';

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

    setCurrentWallpaper() {
        const currentWallpaper = this.getCurrentWallpaper();

        if (currentWallpaper) {
            this.wallpaperService.setWallpaper(currentWallpaper);
        } else {
            this.getWallpaper();
            this.wallpaperService.setWallpaper(this.getCurrentWallpaper()!)
        }
    }

    getWallpaper() {
        this.httpClient.get<any>(`${environment.baseUrl}/unsplash/photo?topic=wallpapers`).subscribe(result => {
            const wallpaper = result.urls.full;

            this.addNewWallpaper(wallpaper);
        })
    }

    getCurrentWallpaper(): string | null {
        const currentWallpaper = this.getPreviousWallpapers().at(-1)

        console.log(currentWallpaper);
        console.log(this.getPreviousWallpapers());
        console.log(localStorage.getItem('previous-wallpapers'))
        return currentWallpaper ?? null;
    }

    getPreviousWallpapers(): string[] {
        const item = localStorage.getItem('previous-wallpapers');

        return item ? JSON.parse(item) : [];
    }

    setPreviousWallpapers(wallpapers: string[]) {
        localStorage.setItem('previous-wallpapers', JSON.stringify(wallpapers));
    }

    addNewWallpaper(wallpaper: string) {
        const previousWallpapers = this.getPreviousWallpapers() ?? [];

        if (previousWallpapers.length >= 10) {
            previousWallpapers.shift();
        }

        this.wallpaperService.setWallpaper(wallpaper);
        previousWallpapers.push(wallpaper);

        this.setPreviousWallpapers(previousWallpapers);
    }
}
