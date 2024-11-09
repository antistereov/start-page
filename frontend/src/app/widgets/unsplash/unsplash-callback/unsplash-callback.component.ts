import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {UnsplashAuthService, UnsplashUserProfile} from '../unsplash-auth/unsplash-auth.service';
import {UnsplashCallbackService} from './unsplash-callback.service';
import {ProgressSpinnerModule} from 'primeng/progressspinner';
import {Message} from 'primeng/message';
import {NgIf} from '@angular/common';

@Component({
  selector: 'app-unsplash-callback',
  standalone: true,
    imports: [
        ProgressSpinnerModule,
        Message,
        NgIf
    ],
  templateUrl: './unsplash-callback.component.html',
  styleUrl: './unsplash-callback.component.css'
})
export class UnsplashCallbackComponent implements OnInit {
    loading: boolean = true;
    successMessage: string = '';
    errorMessage: string = '';

    constructor(
        private route: ActivatedRoute,
        private unsplashAuthService: UnsplashAuthService,
        private router: Router,
    ) {}

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            const code = params['code'];
            const state = params['state'];
            const error = params['error'];

            if (code && state) {
                this.authenticateUser(code, state);
            } else if (error) {
                this.loading = false;
                this.errorMessage = `Authorization failed: ${error}`;
            } else {
                this.loading = false;
                this.errorMessage = 'Authorization code or state not found';
            }
        })
    }

    authenticateUser(code: string, state: string): void {
        this.unsplashAuthService.authenticateCallback(code, state).subscribe({
            next: (profile) => {
                this.successMessage = `Welcome, ${profile.username}!`;
                this.loading = false;
                this.unsplashAuthService.setUserProfile(new UnsplashUserProfile(profile.username, profile.profile_image.medium));
                setTimeout(() => this.router.navigate(['/']), 2000);
            },
            error: () => {
                this.errorMessage = 'Authentication failed. Please try again.';
                this.loading = false;
            }
        });
    }

}
