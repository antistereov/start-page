import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {catchError, map, Observable, of} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    httpClient = inject(HttpClient);
    baseUrl = 'http://localhost:8000';

    login(data: any): Observable<any> {
        const payload = {
            ...data,
            deviceInfoDto: {
                deviceId: this.getDeviceId(),
            }
        };

        return this.httpClient.post<any>(`${this.baseUrl}/auth/login`, payload);
    }

    logout(): Observable<any> {
        return this.httpClient.post(`${this.baseUrl}/auth/logout`, {}).pipe(
            map(() => {
                localStorage.clear();
                sessionStorage.clear();
            })
        );
    }

    isLoggedIn(): Observable<boolean> {
        return this.httpClient.get<{ status: string }>(`${this.baseUrl}/auth/check`).pipe(
            map((response) => response.status === 'authenticated'),
            catchError(() => of(false))
        );
    }

    private getDeviceId(): string {
        let deviceId = localStorage.getItem('device_id');
        if (!deviceId) {
            deviceId = 'device-' + Math.random().toString(36).substring(2) + Date.now().toString(36);
            localStorage.setItem('device_id', deviceId);
        }
        return deviceId;
    }

    refreshToken(): Observable<any> {
        return this.httpClient.post(`${this.baseUrl}/auth/refresh`, { deviceId: this.getDeviceId() });
    }
}
