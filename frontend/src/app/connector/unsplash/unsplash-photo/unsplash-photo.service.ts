import { Injectable } from '@angular/core';
import {Observable} from 'rxjs';
import {environment} from '../../../../environment/environment';
import {UnsplashPhoto} from './unsplash-photo.model';
import {HttpClient, HttpParams} from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})
export class UnsplashPhotoService {

    constructor(private httpClient: HttpClient) {}

    getRandomPhoto(): Observable<UnsplashPhoto> {
        const uri = `${environment.baseUrl}/unsplash/photo`;
        const screenWidth = screen.width;
        const screenHeight = screen.height;

        // TODO: Add search params and quality

        const params = new HttpParams()
            .set('screenWidth', screenWidth.toString())
            .set('screenHeight', screenHeight.toString())

        return this.httpClient.get<UnsplashPhoto>(uri, { params });
    }

    getPhoto(id: string): Observable<UnsplashPhoto> {
        const uri = `${environment.baseUrl}/unsplash/photo/${id}`;
        const screenWidth = screen.width;
        const screenHeight = screen.height;

        const params = new HttpParams()
            .set('screenWidth', screenWidth.toString())
            .set('screenHeight', screenHeight.toString())

        return this.httpClient.get<UnsplashPhoto>(uri, { params });
    }

    likePhoto(id: string): Observable<any> {
        const uri = `${environment.baseUrl}/unsplash/photo/${id}`;

        return this.httpClient.post<any>(uri, null);
    }

    unlikePhoto(id: string): Observable<any> {
        const uri = `${environment.baseUrl}/unsplash/photo/${id}`;

        return this.httpClient.delete(uri);
    }
}
