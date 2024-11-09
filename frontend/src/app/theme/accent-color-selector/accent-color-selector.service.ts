import {Injectable} from '@angular/core';
import {AccentColor} from './accent-color.enum';
import {updatePrimaryPalette} from 'primeng/themes'
import {BehaviorSubject} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class AccentColorSelectorService {
    private accentColorSubject = new BehaviorSubject<AccentColor>(this.getInitialAccentColor());
    accentColor$ = this.accentColorSubject.asObservable();

    setAccentColor(accentColor: AccentColor) {
        this.accentColorSubject.next(accentColor);
        localStorage.setItem('accent-color', accentColor);

        this.applyAccentColor(accentColor);
    }

    setCurrentAccentColor() {
        const accentColor = (localStorage.getItem('accent-color') as AccentColor) || AccentColor.Indigo;
        this.applyAccentColor(accentColor);
    }

    private getInitialAccentColor(): AccentColor {
        return (localStorage.getItem('accent-color') as AccentColor) || AccentColor.Indigo;
    }

    private applyAccentColor(accentColor: AccentColor) {
        updatePrimaryPalette({
            50: this.createString(accentColor, 50),
            100: this.createString(accentColor, 100),
            200: this.createString(accentColor, 200),
            300: this.createString(accentColor, 300),
            400: this.createString(accentColor, 400),
            500: this.createString(accentColor, 500),
            600: this.createString(accentColor, 600),
            700: this.createString(accentColor, 700),
            800: this.createString(accentColor, 800),
            900: this.createString(accentColor, 900),
            950: this.createString(accentColor, 950),
        });
    }

    private createString(accentColor: AccentColor, level: number): string {
        return `{${accentColor}.${level}}`
    }
}
