import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../../environment/environment';
import {BehaviorSubject, Observable} from 'rxjs';

@Injectable({
    providedIn: 'root'
})
export class UnsplashAuthService {
    private userProfileSubject = new BehaviorSubject<UnsplashUserProfile | null>(null);
    userProfile$ = this.userProfileSubject.asObservable();

    constructor(private httpClient: HttpClient) {}

    baseUrl = 'http://localhost:8000';

    getUserProfile(): void {
        this.httpClient.get<any>(`${environment.baseUrl}/auth/unsplash/me`)
            .subscribe(result => {
                if (result.username) {
                    const profile = new UnsplashUserProfile(result.username, result.profile_image.medium);
                    this.userProfileSubject.next(profile);
                } else {
                    this.userProfileSubject.next(null);
                }
            });
    }

    setUserProfile(userProfile: UnsplashUserProfile): void {
        this.userProfileSubject.next(userProfile);
    }

    login() {
        this.httpClient.get<any>(`${this.baseUrl}/auth/unsplash`).subscribe(result => {
            window.location.href = result.url;
        })
    }

    logout(): void {
        this.httpClient.delete(`${this.baseUrl}/auth/unsplash`).subscribe(() => {
            this.userProfileSubject.next(null);
        })
    }

    authenticateCallback(code: string, state: string): Observable<any> {
        const params = new HttpParams()
            .set('code', code)
            .set('state', state)
        return this.httpClient.get<any>(`${environment.baseUrl}/auth/unsplash/callback`, { params })
    }
}

export class UnsplashUserProfile {
    constructor(
        public username: string,
        public profileImage: string | undefined
    ) {}
}

