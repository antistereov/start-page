import { Component } from '@angular/core';
import {DynamicGridComponent} from "../../components/shared/dynamic-grid/dynamic-grid.component";
import {RouterOutlet} from "@angular/router";
import {UnsplashWallpaperComponent} from "../../connector/unsplash/unsplash-wallpaper/unsplash-wallpaper.component";

@Component({
  selector: 'app-home',
  standalone: true,
    imports: [
        DynamicGridComponent,
        RouterOutlet,
        UnsplashWallpaperComponent
    ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
