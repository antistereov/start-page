import {Component, OnInit} from '@angular/core';
import {UnsplashWallpaperService} from './unsplash-wallpaper.service';
import {ButtonModule} from 'primeng/button';
import {NgIf} from '@angular/common';
import {UnsplashPhoto} from '../unsplash-photo/unsplash-photo.model';
import {environment} from '../../../../environment/environment';

@Component({
  selector: 'app-unsplash-widget',
  standalone: true,
    imports: [
        ButtonModule,
        NgIf
    ],
  templateUrl: './unsplash-wallpaper.component.html',
  styleUrl: './unsplash-wallpaper.component.css'
})
export class UnsplashWallpaperComponent implements OnInit {
    isLast: boolean = false;
    isFirst: boolean = false;
    currentWallpaper: UnsplashPhoto | undefined = undefined;
    likedByUser: boolean | undefined = undefined;
    userName: string | undefined = undefined;
    userLink: string | undefined = undefined;
    photoLink: string | undefined = undefined;

    unsplashReferral = `?utm_source=${environment.appName}&utm_medium=referral`;

    constructor(
        private unsplashWallpaperService: UnsplashWallpaperService,
    ) {}

    ngOnInit() {
        this.unsplashWallpaperService.isLast$.subscribe(isLast => {
            this.isLast = isLast;
        });
        this.unsplashWallpaperService.isFirst$.subscribe(isFirst => {
            this.isFirst = isFirst;
        });
        this.unsplashWallpaperService.currentWallpaper$.subscribe(currentWallpaper => {
            this.currentWallpaper = currentWallpaper;
            this.likedByUser = currentWallpaper?.likedByUser;
            this.userName = currentWallpaper?.user.name;
            this.userLink = currentWallpaper?.user.link;
            this.photoLink = currentWallpaper?.links.html;
        })

        this.unsplashWallpaperService.setCurrentWallpaper().then();
    }

    nextWallpaper() {
        this.unsplashWallpaperService.setNextWallpaper().then();
    }

    previousWallpaper() {
        this.unsplashWallpaperService.setPreviousWallpaper().then()
    }

    toggleLike() {
        this.unsplashWallpaperService.toggleLike()
    }
}
