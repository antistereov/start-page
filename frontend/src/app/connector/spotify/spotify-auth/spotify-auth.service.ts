import { Injectable } from '@angular/core';
import {BehaviorSubject, map, Observable} from 'rxjs';
import {HttpClient, HttpParams} from '@angular/common/http';
import {environment} from '../../../../environment/environment';

@Injectable({
    providedIn: 'root'
})
export class SpotifyAuthService {
    private userProfileSubject = new BehaviorSubject<SpotifyUserProfile | null>(null);
    userProfile$ = this.userProfileSubject.asObservable();

    constructor(private httpClient: HttpClient) {}

    getUserProfile(): void {
        this.httpClient.get<any>(`${environment.baseUrl}/auth/spotify/me`)
            .subscribe(result => {
                console.log(result);
                if (result.display_name && result.product) {
                    const profile = new SpotifyUserProfile(result.display_name, result.images.at(0).url, result.product);
                    this.userProfileSubject.next(profile)
                } else {
                    this.userProfileSubject.next(null);
                }
            })
    }

    setUserProfile(userProfile: SpotifyUserProfile): void {
        this.userProfileSubject.next(userProfile);
    }

    connect() {
        this.httpClient.get<any>(`${environment.baseUrl}/auth/spotify`).subscribe(result => {
            window.location.href = result.url;
        })
    }

    disconnect() {
        this.httpClient.delete(`${environment.baseUrl}/auth/spotify`).subscribe(() => {
            this.userProfileSubject.next(null);
        })
    }

    authenticateCallback(code: string, state: string): Observable<any> {
        const params = new HttpParams()
            .set('code', code)
            .set('state', state)
        return this.httpClient.get<any>(`${environment.baseUrl}/auth/spotify/callback`, { params });
    }

    getAccessToken(): Observable<string> {
        return this.httpClient.get<any>(`${environment.baseUrl}/auth/spotify/access-token`).pipe(
            map(value => value.accessToken)
        )

    }

}

export class SpotifyUserProfile {
    constructor(
        public username: string,
        public profileImage: string | null,
        public product: string,
    ) {
    }
}
