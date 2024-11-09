import {Inject, Injectable, Renderer2, RendererFactory2} from '@angular/core';
import {DOCUMENT} from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class WallpaperService {
    private renderer: Renderer2;

    constructor(
        rendererFactory: RendererFactory2,
        @Inject(DOCUMENT) private document: Document
    ) {
        this.renderer = rendererFactory.createRenderer(null, null);
    }

    setWallpaper(url: string) {
        const body = this.document.body;
        this.renderer.setStyle(body, 'backgroundImage', `url(${url})`);
    }

}
