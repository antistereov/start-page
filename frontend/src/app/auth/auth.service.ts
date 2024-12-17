import {inject, Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {catchError, map, Observable, of} from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    httpClient = inject(HttpClient);
    baseUrl = 'http://localhost:8000/api';

    login(data: any): Observable<any> {
        return this.httpClient.post<any>(`${this.baseUrl}/auth/login`, data);
    }

    logout(): Observable<any> {
        return this.httpClient.post(`${this.baseUrl}/auth/logout`, {});
    }

    isLoggedIn(): Observable<boolean> {
        return this.httpClient.get<{ status: string }>(`${this.baseUrl}/auth/check`).pipe(
            map((response) => response.status === 'authenticated'),
            catchError(() => of(false))
        );
    }
}



