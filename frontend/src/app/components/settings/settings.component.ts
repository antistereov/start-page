import {Component, OnInit} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {SidebarModule} from 'primeng/sidebar';
import {ThemeSelectorComponent} from './appearance-settings/theme-selector/theme-selector.component';
import {AccentColorSelectorComponent} from './appearance-settings/accent-color-selector/accent-color-selector.component';
import {NgIf} from '@angular/common';
import {AvatarModule} from 'primeng/avatar';
import {SettingsService} from './settings.service';
import {UserSettingsComponent} from './user-settings/user-settings.component';
import {DividerModule} from 'primeng/divider';
import {AppearanceSettingsComponent} from './appearance-settings/appearance-settings.component';
import {UnsplashAuthComponent} from '../../connector/unsplash/unsplash-auth/unsplash-auth.component';
import {Drawer} from 'primeng/drawer';
import {SpotifyAuthComponent} from '../../connector/spotify/spotify-auth/spotify-auth.component';

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
        Drawer,
        SpotifyAuthComponent
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
