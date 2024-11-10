import {Component, OnInit} from '@angular/core';
import {UnsplashWidgetService} from './unsplash-widget.service';
import {ButtonModule} from 'primeng/button';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-unsplash-widget',
  standalone: true,
    imports: [
        ButtonModule,
        NgIf
    ],
  templateUrl: './unsplash-widget.component.html',
  styleUrl: './unsplash-widget.component.css'
})
export class UnsplashWidgetComponent implements OnInit {
    isLast: boolean = false;
    isFirst: boolean = false;

    constructor(
        private unsplashWidgetService: UnsplashWidgetService,
    ) {}

    ngOnInit() {
        this.unsplashWidgetService.isLast$.subscribe((isLast) => {
            this.isLast = isLast;
        });
        this.unsplashWidgetService.isFirst$.subscribe((isFirst) => {
            this.isFirst = isFirst;
        });

        this.unsplashWidgetService.setCurrentWallpaper();
    }

    nextWallpaper() {
        this.unsplashWidgetService.setNextWallpaper();
    }

    previousWallpaper() {
        this.unsplashWidgetService.setPreviousWallpaper()
    }
}
