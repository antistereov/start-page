import {Component, OnInit} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {SidebarModule} from 'primeng/sidebar';
import {ThemeSelectorComponent} from '../theme/theme-selector/theme-selector.component';
import {AccentColorSelectorComponent} from '../theme/accent-color-selector/accent-color-selector.component';
import {NgIf} from '@angular/common';
import {AvatarModule} from 'primeng/avatar';
import {SettingsService} from './settings.service';
import {UserSettingsComponent} from './user-settings/user-settings.component';
import {DividerModule} from 'primeng/divider';
import {AppearanceSettingsComponent} from './appearance-settings/appearance-settings.component';
import {UnsplashAuthComponent} from '../widgets/unsplash/unsplash-auth/unsplash-auth.component';
import {Drawer} from 'primeng/drawer';

@Component({
  selector: 'app-settings',
  standalone: true,
    imports: [
        ButtonModule,
        SidebarModule,
        ThemeSelectorComponent,
        AccentColorSelectorComponent,
        NgIf,
        AvatarModule,
        UserSettingsComponent,
        DividerModule,
        AppearanceSettingsComponent,
        UnsplashAuthComponent,
        Drawer
    ],
  templateUrl: './settings.component.html',
  styleUrl: './settings.component.css'
})
export class SettingsComponent implements OnInit {
    isVisible: boolean = false;

    constructor(private settingsService: SettingsService) {
    }

    ngOnInit() {
        this.settingsService.isVisible$.subscribe(
            (visible) => (this.isVisible = visible)
        );
    }

    open() {
        this.settingsService.open();
    }
}
