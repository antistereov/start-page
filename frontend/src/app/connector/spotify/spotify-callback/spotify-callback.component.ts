import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from '@angular/router';
import {SpotifyAuthService, SpotifyUserProfile} from '../spotify-auth/spotify-auth.service';
import {Message} from 'primeng/message';
import {NgIf} from '@angular/common';
import {ProgressSpinnerModule} from 'primeng/progressspinner';

@Component({
  selector: 'app-spotify-callback',
  standalone: true,
    imports: [
        Message,
        NgIf,
        ProgressSpinnerModule
    ],
  templateUrl: './spotify-callback.component.html',
  styleUrl: './spotify-callback.component.css'
})
export class SpotifyCallbackComponent implements OnInit {
    loading: boolean = true;
    successMessage: string = '';
    errorMessage: string = '';

    constructor(
        private route: ActivatedRoute,
        private spotifyAuthService: SpotifyAuthService,
        private router: Router,
    ) {}

    ngOnInit() {
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
        });
    }

    authenticateUser(code: string, state: string): void {
        this.spotifyAuthService.authenticateCallback(code, state).subscribe({
            next: (profile) => {
                this.successMessage = `Welcome, ${profile.display_name}!`;
                this.loading = false;
                this.spotifyAuthService.setUserProfile(new SpotifyUserProfile(profile.display_name, profile.images.at(0).url, profile.product));
                setTimeout(() => this.router.navigate(['/']), 2000);
            }
        })
    }

}
