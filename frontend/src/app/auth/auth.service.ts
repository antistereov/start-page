import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {tap} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    httpClient = inject(HttpClient);
    baseUrl = 'http://localhost:8000/api';

    login(data: any) {
        return this.httpClient.post<any>(`${this.baseUrl}/auth/login`, data)
            .pipe(tap(result => {
                this.setSession(result);
            }));
    }

    private setSession(authResult: AuthResult): void {
        const expiresAt = Date.now() + authResult.expiresIn * 1000;

        localStorage.setItem('accessToken', authResult.accessToken);
        localStorage.setItem('expiresAt', JSON.stringify(expiresAt.valueOf()))
    }

    public logout(): void {
        if (typeof localStorage !== 'undefined') {
            localStorage.removeItem('token');
            localStorage.removeItem('expiration');
        }
    }

    getAccessToken(): string | null {
        if (typeof localStorage !== 'undefined') {
            return localStorage.getItem('accessToken');
        }
        return null;
    }

    isLoggedIn(): boolean {
        if (typeof localStorage !== 'undefined') {
            const expiration = this.getExpiration();
            if (expiration) {
                return Date.now() < expiration;
            }
        }
        return false;
    }

    getExpiration(): number | null {
        if (typeof localStorage !== 'undefined') {
            const expiration = localStorage.getItem('expiresAt');
            return expiration ? parseInt(expiration, 10) : null;
        }
        return null;
    }
}

export class AuthResult {

    constructor(
        public accessToken: string,
        public expiresIn: number,
    ) {}
}



