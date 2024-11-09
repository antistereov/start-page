import { Injectable } from '@angular/core';
import {UnsplashAuthService} from '../unsplash-auth/unsplash-auth.service';

@Injectable({
    providedIn: 'root'
})
export class UnsplashCallbackService {

    constructor(private unsplashAuthService: UnsplashAuthService) {}


}
