import {Component, ElementRef, OnDestroy, Renderer2} from '@angular/core';
import {Subscription} from 'rxjs';
import {WallpaperService} from './wallpaper.service';

@Component({
  selector: 'app-wallpaper',
  standalone: true,
  imports: [],
  templateUrl: './wallpaper.component.html',
  styleUrl: './wallpaper.component.css'
})
export class WallpaperComponent implements OnDestroy {
    private backgroundSubscription: Subscription;
    private isBackground1Active = true;

    constructor(
        private renderer: Renderer2,
        private el: ElementRef,
        private wallpaperService: WallpaperService
    ) {
        this.backgroundSubscription = this.wallpaperService.backgroundUrl$.subscribe(url => {
            if (url) {
                this.fadeBackground(url);
            }
        });
    }

    private fadeBackground(url: string) {
        const background1 = this.el.nativeElement.querySelector('#background1');
        const background2 = this.el.nativeElement.querySelector('#background2');

        const activeBackground = this.isBackground1Active ? background1 : background2;
        const inactiveBackground = this.isBackground1Active ? background2 : background1;

        const tempImg = new Image();
        tempImg.src = url;

        tempImg.onload = () => {
            // Set the new image on the inactive background layer
            this.renderer.setStyle(inactiveBackground, 'backgroundImage', `url(${url})`);

            // Fade in the new background and fade out the current one simultaneously
            this.renderer.setStyle(inactiveBackground, 'opacity', '1');
            this.renderer.setStyle(activeBackground, 'opacity', '0');

            // Toggle the active background for the next transition
            this.isBackground1Active = !this.isBackground1Active;
        };

        tempImg.onerror = () => {
            console.error("Failed to load background image:", url);
            // Optionally, you could retry loading the image or handle the error in other ways.
        };
    }

    ngOnDestroy() {
        this.backgroundSubscription.unsubscribe();
    }
}
