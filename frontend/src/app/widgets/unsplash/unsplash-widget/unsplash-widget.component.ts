import {Component, Inject, OnInit, Renderer2, RendererFactory2} from '@angular/core';
import {DOCUMENT} from '@angular/common';
import {UnsplashWidgetService} from './unsplash-widget.service';
import {getWorkspace} from '@angular/cli/src/utilities/config';
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
export class UnsplashWidgetComponent implements OnInit {
    private renderer: Renderer2;

    constructor(
        private unsplashWidgetService: UnsplashWidgetService,
        rendererFactory: RendererFactory2,
        @Inject(DOCUMENT) private document: Document
    ) {
        this.renderer = rendererFactory.createRenderer(null, null);
    }

    ngOnInit() {
        this.unsplashWidgetService.getWallpaper();
        this.unsplashWidgetService.currentWallpaper$.subscribe(result => {
                if (result) {
                    this.setWallpaper(result?.toString())
                }
            }
        )
    }

    setWallpaper(url: string) {
        const body = this.document.body;
        this.renderer.setStyle(body, 'backgroundImage', `url(${url})`);// Optional: Set to cover the entire screen
        this.renderer.setStyle(body, 'backgroundPosition', 'center');
    }

    newWallpaper() {
        this.unsplashWidgetService.getWallpaper();
    }

}
