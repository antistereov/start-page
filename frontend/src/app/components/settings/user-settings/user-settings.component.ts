import {Component, OnInit} from '@angular/core';
import {AvatarModule} from "primeng/avatar";
import {ButtonModule} from "primeng/button";
import {AsyncPipe, NgIf} from "@angular/common";
import {Router} from '@angular/router';
import {AuthService} from '../../../auth/auth.service';
import {SettingsService} from '../settings.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-user-settings',
  standalone: true,
    imports: [
        AvatarModule,
        ButtonModule,
        NgIf,
        AsyncPipe
    ],
  templateUrl: './user-settings.component.html',
  styleUrl: './user-settings.component.css'
})
export class UserSettingsComponent implements OnInit {
    isLoggedIn$!: Observable<boolean>;

    constructor(
        private router: Router,
        private authService: AuthService,
        private settingsService: SettingsService
    ) {}

    ngOnInit() {
        this.isLoggedIn$ = this.authService.isLoggedIn();
    }

    redirectToLogin() {
        this.settingsService.close();
        this.router.navigate(['/login']).then();
    }

    logout() {
        this.authService.logout();
    }
}
