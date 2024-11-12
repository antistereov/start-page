import { Routes } from '@angular/router';
import {LoginComponent} from './pages/login/login.component';
import {AdminComponent} from './pages/admin/admin.component';
import {authGuard} from './auth/auth.guard';
import {UnsplashCallbackComponent} from './connector/unsplash/unsplash-callback/unsplash-callback.component';
import {SpotifyCallbackComponent} from './connector/spotify/spotify-callback/spotify-callback.component';

export const routes: Routes = [
    // { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'login', component: LoginComponent },
    { path: 'admin', component: AdminComponent, canActivate: [authGuard] },
    { path: 'callback/unsplash', component: UnsplashCallbackComponent },
    { path: 'callback/spotify', component: SpotifyCallbackComponent }
];
