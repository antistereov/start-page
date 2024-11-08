import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Theme} from './theme.enum';

@Injectable({
    providedIn: 'root'
})
export class ThemeService {
    private themeSubject = new BehaviorSubject<Theme>(this.getInitialTheme());
    theme$ = this.themeSubject.asObservable();

    constructor() {
        this.applyTheme(this.themeSubject.value);
        this.detectColorScheme();
    }

    private detectColorScheme() {
        const darkThemeMq = window.matchMedia('(prefers-color-scheme: dark');
        darkThemeMq.addEventListener('change', (e) => {
            if (this.themeSubject.value === Theme.System) {
                this.applyTheme(e.matches ? Theme.Dark : Theme.Light);
            }
        });
    }

    private getInitialTheme(): Theme {
        return (localStorage.getItem('theme') as Theme) || Theme.System;
    }

    setTheme(theme: Theme) {
        this.themeSubject.next(theme);
        localStorage.setItem('theme', theme);

        this.applyTheme(theme);
    }

    private getSystemTheme(): Theme.Dark | Theme.Light {
        return window.matchMedia('(prefers-color-scheme: dark)').matches ? Theme.Dark : Theme.Light;
    }

    private applyTheme(theme: Theme) {
        const htmlElement = document.querySelector('html');
        const bodyElement = document.body;

        htmlElement?.classList.remove('dark-mode');
        bodyElement?.classList.remove('dark-mode');

        switch (theme) {
            case Theme.Light: break;
            case Theme.Dark:
                htmlElement?.classList.add('dark-mode');
                bodyElement?.classList.add('dark-mode');
                break;
            case Theme.System:
                const systemTheme = this.getSystemTheme();
                if (systemTheme === Theme.Dark) {
                    htmlElement?.classList.add('dark-mode');
                    bodyElement?.classList.add('dark-mode');
                }
        }
    }

}

