import { Component } from '@angular/core';
import {DynamicGridComponent} from "../../components/shared/dynamic-grid/dynamic-grid.component";
import {RouterOutlet} from "@angular/router";
import {UnsplashWidgetComponent} from "../../connector/unsplash/unsplash-widget/unsplash-widget.component";

@Component({
  selector: 'app-home',
  standalone: true,
    imports: [
        DynamicGridComponent,
        RouterOutlet,
        UnsplashWidgetComponent
    ],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css'
})
export class HomeComponent {

}
