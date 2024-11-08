import { Component } from '@angular/core';
import {AvatarModule} from "primeng/avatar";
import {ButtonModule} from "primeng/button";
import {NgIf} from "@angular/common";
import {Router} from '@angular/router';
import {AuthService} from '../../auth/auth.service';
import {SettingsService} from '../settings.service';

@Component({
  selector: 'app-user-settings',
  standalone: true,
    imports: [
        AvatarModule,
        ButtonModule,
        NgIf
    ],
  templateUrl: './user-settings.component.html',
  styleUrl: './user-settings.component.css'
})
export class UserSettingsComponent {

    constructor(
        private router: Router,
        private authService: AuthService,
        private settingsService: SettingsService
    ) {}

    redirectToLogin() {
        this.settingsService.close();
        this.router.navigate(['/login']).then();
    }

    isLoggedIn(): boolean {
        return this.authService.isLoggedIn();
    }

    logout() {
        this.authService.logout();
    }
}
