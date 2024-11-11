import {Component, OnInit} from '@angular/core';
import {Theme} from './theme.enum';
import {ThemeSelectorService} from './theme-selector.service';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';

@Component({
  selector: 'app-theme-selector',
  standalone: true,
    imports: [
        FormsModule,
        Select
    ],
  templateUrl: './theme-selector.component.html',
  styleUrl: './theme-selector.component.css'
})
export class ThemeSelectorComponent implements OnInit {
    themeOptions = [
        { label: 'System', value: Theme.System },
        { label: 'Light', value: Theme.Light },
        { label: 'Dark', value: Theme.Dark }
    ];
    selectedTheme: Theme = Theme.System;

    constructor(private themeService: ThemeSelectorService) {}

    ngOnInit() {
        this.themeService.theme$.subscribe((theme) => (this.selectedTheme = theme));
    }

    onThemeChange(event: any) {
        this.themeService.setTheme(event.value as Theme);
    }

}
