import {Component} from '@angular/core';
import {UnsplashWidgetService} from './unsplash-widget.service';
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'app-unsplash-widget',
  standalone: true,
    imports: [
        ButtonModule
    ],
  templateUrl: './unsplash-widget.component.html',
  styleUrl: './unsplash-widget.component.css'
})
export class UnsplashWidgetComponent {

    constructor(
        private unsplashWidgetService: UnsplashWidgetService,
    ) {}

    newWallpaper() {
        this.unsplashWidgetService.getWallpaper();
    }
}
