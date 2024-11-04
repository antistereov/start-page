import {Component, OnInit} from '@angular/core';
import { RouterOutlet } from '@angular/router';
import {PrimeNGConfig} from 'primeng/api';
import {ButtonModule} from 'primeng/button';
import {ToggleButton} from 'primeng/togglebutton'
import {DefaultTheme} from '../themes/default-theme';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterOutlet, ButtonModule, ToggleButton],
    templateUrl: './app.component.html',
    styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
    title = 'frontend';

    constructor(private primeConfig: PrimeNGConfig) {
        this.primeConfig.theme.set({
            preset: DefaultTheme,
            options: {
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

    toggleDarkMode() {
        const element = document.querySelector('html');
        element!!.classList.toggle('dark-mode');
        document.body.classList.toggle('dark-mode');
    }

}

