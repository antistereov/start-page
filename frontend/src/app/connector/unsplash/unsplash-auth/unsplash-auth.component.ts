import {Component, OnInit} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {UnsplashAuthService, UnsplashUserProfile} from './unsplash-auth.service';
import {NgIf} from '@angular/common';
import {AvatarModule} from 'primeng/avatar';

@Component({
  selector: 'app-unsplash-auth',
  standalone: true,
    imports: [
        ButtonModule,
        NgIf,
        AvatarModule
    ],
  templateUrl: './unsplash-auth.component.html',
  styleUrl: './unsplash-auth.component.css'
})
export class UnsplashAuthComponent implements OnInit {
    userProfile: UnsplashUserProfile | null = null;
    isLoggedIn: boolean = false;

    constructor(private unsplashAuthService: UnsplashAuthService) {}

    ngOnInit() {
        this.unsplashAuthService.getUserProfile();
        this.unsplashAuthService.userProfile$.subscribe(profile => {
            this.userProfile = profile;
            this.isLoggedIn = !!profile;
        })
    }

    connect() {
        this.unsplashAuthService.connect();
    }

    disconnect() {
        this.unsplashAuthService.disconnect();
    }
}
