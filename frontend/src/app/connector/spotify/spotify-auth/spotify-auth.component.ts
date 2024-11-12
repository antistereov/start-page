import {Component, OnInit} from '@angular/core';
import {AvatarModule} from "primeng/avatar";
import {ButtonModule} from "primeng/button";
import {NgIf} from "@angular/common";
import {UnsplashUserProfile} from '../../unsplash/unsplash-auth/unsplash-auth.service';
import {SpotifyAuthService} from './spotify-auth.service';

@Component({
    selector: 'app-spotify-auth',
    standalone: true,
        imports: [
            AvatarModule,
            ButtonModule,
            NgIf
        ],
    templateUrl: './spotify-auth.component.html',
    styleUrl: './spotify-auth.component.css'
})
export class SpotifyAuthComponent implements OnInit {
    userProfile: UnsplashUserProfile | null = null;
    isLoggedIn: boolean = false;

    constructor(private spotifyAuthService: SpotifyAuthService) {}

    ngOnInit() {
        this.spotifyAuthService.getUserProfile();
        this.spotifyAuthService.userProfile$.subscribe(profile => {
            this.userProfile = profile;
            this.isLoggedIn = !!profile;
        })
    }

    connect() {
        this.spotifyAuthService.connect();
    }

    disconnect() {
        this.spotifyAuthService.disconnect();
    }
    }
