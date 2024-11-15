import { Injectable } from '@angular/core';
import {map, Observable} from 'rxjs';
import {environment} from '../../../../environment/environment';
import {FullWithLiked, toUnsplashPhoto, UnsplashPhoto} from './unsplash-photo.model';
import {HttpClient} from '@angular/common/http';
import {RandomParams} from 'unsplash-js/dist/methods/photos';

@Injectable({
  providedIn: 'root'
})
export class UnsplashPhotoService {

    constructor(private httpClient: HttpClient) {}

    getRandomPhoto(
        randomParams: RandomParams,
    ): Observable<UnsplashPhoto> {


        const uri = `${environment.baseUrl}/unsplash/photo`;

        return this.httpClient.get<FullWithLiked>(uri, { params: paramsToHttpParams(randomParams) }).pipe(
            map(result => {
                return toUnsplashPhoto(result);
            })
        );
    }

    getPhoto(id: string): Observable<UnsplashPhoto> {
        const uri = `${environment.baseUrl}/unsplash/photo/${id}`

        return this.httpClient.get<FullWithLiked>(uri).pipe(
            map(result => {
                return toUnsplashPhoto(result);
            })
        )
    }

    likePhoto(id: string): Observable<UnsplashPhoto> {
        console.log(`Like photo ${id}`)
        const uri = `${environment.baseUrl}/unsplash/photo/${id}`;

        return this.httpClient.post<FullWithLiked>(uri, null).pipe(
            map(result => {
                return toUnsplashPhoto(result);
            })
        )
    }

    unlikePhoto(id: string): Observable<any> {
        const uri = `${environment.baseUrl}/unsplash/photo/${id}`;

        return this.httpClient.delete(uri);
    }
}

function paramsToHttpParams(params: RandomParams): { [param: string]: string } {
    return Object.entries(params)
        .reduce((acc, [key, value]) => {
            if (value !== undefined && value !== null) {
                acc[key] = value.toString();
            }
            return acc;
        }, {} as { [param: string]: string });
}
