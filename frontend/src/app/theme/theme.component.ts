import {Component, OnInit} from '@angular/core';
import {Theme} from './theme.enum';
import {ThemeService} from './theme.service';
import {DropdownModule} from 'primeng/dropdown';
import {FormsModule} from '@angular/forms';

@Component({
  selector: 'app-theme',
  standalone: true,
    imports: [
        DropdownModule,
        FormsModule
    ],
  templateUrl: './theme.component.html',
  styleUrl: './theme.component.css'
})
export class ThemeComponent implements OnInit {
    themeOptions = [
        { label: 'System', value: Theme.System },
        { label: 'Light', value: Theme.Light },
        { label: 'Dark', value: Theme.Dark }
    ];
    selectedTheme: Theme = Theme.System;

    constructor(private themeService: ThemeService) {}

    ngOnInit() {
        this.themeService.theme$.subscribe((theme) => (this.selectedTheme = theme));
    }

    onThemeChange(event: any) {
        this.themeService.setTheme(event.value as Theme);
    }

}
