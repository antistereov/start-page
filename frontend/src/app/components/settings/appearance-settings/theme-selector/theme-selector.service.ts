import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Theme} from './theme.enum';

@Injectable({
    providedIn: 'root'
})
export class ThemeSelectorService {
    private themeSubject = new BehaviorSubject<Theme>(this.getInitialTheme());
    theme$ = this.themeSubject.asObservable();
    private isDarkModeSubject = new BehaviorSubject<boolean>(false);
    isDarkModeSubject$ = this.isDarkModeSubject.asObservable();

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

    private themeIsDark(theme: Theme): boolean {
        switch (theme) {
            case Theme.Light:
                return false;
            case Theme.Dark:
                return true;
            case Theme.System:
                const systemTheme = this.getSystemTheme();
                return systemTheme === Theme.Dark;
        }
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
        bodyElement.classList.remove('dark-mode');

        if (this.themeIsDark(theme)) {
            this.isDarkModeSubject.next(true);
            htmlElement?.classList.add('dark-mode');
            bodyElement.classList.add('dark-mode');
        } else {
            this.isDarkModeSubject.next(false);
        }
    }

}

