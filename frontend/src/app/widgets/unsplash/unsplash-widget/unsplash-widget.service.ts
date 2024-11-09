import { Injectable } from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Wallpaper} from './wallpaper.model';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../../../environment/environment';

@Injectable({
    providedIn: 'root'
})
export class UnsplashWidgetService {
    private currentWallpaperSubject = new BehaviorSubject<string | undefined>(undefined);
    currentWallpaper$ = this.currentWallpaperSubject.asObservable();

    constructor(private httpClient: HttpClient) {}

    getWallpaper() {
        this.httpClient.get<any>(`${environment.baseUrl}/unsplash/photo`).subscribe(result => {
            const wallpaper = result.urls.full;

            const previousWallpapers = localStorage.getItem('previous-wallpapers')
                ?.replace("[", "")
                ?.replace("]", "")
                .split(",") ?? [];

            previousWallpapers.push(wallpaper);

            localStorage.setItem('previous-wallpapers', previousWallpapers.toString())

            this.currentWallpaperSubject.next(previousWallpapers.at(-1)!)
        })
    }


}
