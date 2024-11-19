import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {PrimeNGConfig} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {ToggleButton} from 'primeng/togglebutton'
import {ThemeSelectorComponent} from './components/settings/appearance-settings/theme-selector/theme-selector.component';
import {AccentColorSelectorComponent} from './components/settings/appearance-settings/accent-color-selector/accent-color-selector.component';
import {SettingsComponent} from './components/settings/settings.component';
import {Aura} from 'primeng/themes/aura';
import {UnsplashWallpaperComponent} from './connector/unsplash/unsplash-wallpaper/unsplash-wallpaper.component';
import {DynamicGridComponent} from './components/shared/dynamic-grid/dynamic-grid.component';
import {SpotifyPlaybackComponent} from './connector/spotify/spotify-playback/spotify-playback.component';
import {WallpaperComponent} from './components/shared/wallpaper/wallpaper.component';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [
        RouterOutlet,
        ButtonModule,
        ToggleButton,
        ThemeSelectorComponent,
        AccentColorSelectorComponent,
        SettingsComponent,
        UnsplashWallpaperComponent,
        DynamicGridComponent,
        SpotifyPlaybackComponent,
        WallpaperComponent
    ],
    templateUrl: './app.component.html',
    styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
    title = 'frontend';

    constructor(
        private primeConfig: PrimeNGConfig,
    ) {
        this.primeConfig.theme.set({
            options: {
                preset: Aura,
                prefix: 'p',
                darkModeSelector: '.dark-mode',
                cssLayer: {
                    name: 'primeng',
                    order: 'tailwind-base, primeng, tailwind-utilities'
                },
            }
        })
    }

    ngOnInit() {
        this.primeConfig.ripple.set(true);
    }
}

