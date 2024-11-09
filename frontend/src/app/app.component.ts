import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {PrimeNGConfig} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {ToggleButton} from 'primeng/togglebutton'
import {ThemeSelectorComponent} from './theme/theme-selector/theme-selector.component';
import {AccentColorSelectorComponent} from './theme/accent-color-selector/accent-color-selector.component';
import {SettingsComponent} from './settings/settings.component';
import {Aura} from 'primeng/themes/aura';
import {UnsplashWidgetComponent} from './widgets/unsplash/unsplash-widget/unsplash-widget.component';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterOutlet, ButtonModule, ToggleButton, ThemeSelectorComponent, AccentColorSelectorComponent, SettingsComponent, UnsplashWidgetComponent],
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

