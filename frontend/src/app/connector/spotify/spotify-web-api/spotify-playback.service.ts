import { Injectable } from '@angular/core';
import {HttpClient, HttpParams} from '@angular/common/http';
import {BehaviorSubject, Observable} from 'rxjs';
import {PlaybackState} from '@spotify/web-api-ts-sdk';
import {environment} from '../../../../environment/environment';

@Injectable({
    providedIn: 'root'
})
export class SpotifyPlaybackService {
    private currentlyPlayingSubject = new BehaviorSubject<PlaybackState | null>(null);
    currentlyPlaying$ = this.currentlyPlayingSubject.asObservable();

    private intervalId: any = null;
    private intervalTime: number = 5000;

    constructor(private httpClient: HttpClient) {
        this.getCurrentlyPlaying();
    }

    startPolling(): void {
        if (this.intervalId) {
            return;
        }

        this.intervalId = setInterval(() => {
            this.getCurrentlyPlaying()
        },  this.intervalTime);
    }

    stopPolling(): void {
        if (this.intervalId) {
            clearInterval(this.intervalId);
            this.intervalId = null;
        }
    }

    getCurrentlyPlaying() {
        const uri = `${environment.baseUrl}/spotify/me/player/currently_playing`;

        this.httpClient.get<PlaybackState>(uri).subscribe(result => {
            this.currentlyPlayingSubject.next(result);
        });
    }

    skipToNext(deviceId: string | null = null): Observable<string> {
        const uri = `${environment.baseUrl}/spotify/me/player/next`;

        const params = new HttpParams();

        if (deviceId) {
            params.append('device_id', deviceId)
        }

        return this.httpClient.post<string>(uri, { params });
    }

    skipToPrevious(deviceId: string | null = null): Observable<string> {
        const uri = `${environment.baseUrl}/spotify/me/player/previous`;

        const params = new HttpParams();

        if (deviceId) {
            params.append('device_id', deviceId)
        }

        return this.httpClient.post<string>(uri, { params });
    }

    seekToPosition(positionMs: number, deviceId: string | null = null): Observable<string> {
        const uri = `${environment.baseUrl}/spotify/me/player/seek`;

        const params = new HttpParams();

        if (deviceId) {
            params.append('device_id', deviceId)
        }

        return this.httpClient.post<string>(uri, { params });
    }




}
