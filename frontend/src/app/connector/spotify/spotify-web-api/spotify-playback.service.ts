import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {BehaviorSubject} from 'rxjs';
import {PlaybackState} from '@spotify/web-api-ts-sdk';
import {environment} from '../../../../environment/environment';

@Injectable({
    providedIn: 'root'
})
export class SpotifyPlaybackService {
    private currentlyPlayingSubject = new BehaviorSubject<PlaybackState | null>(null);
    currentlyPlaying$ = this.currentlyPlayingSubject.asObservable();

    private intervalId: any = null;
    private intervalTime: number = 3000;

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

    private getCurrentlyPlaying() {
        const uri = `${environment.baseUrl}/spotify/me/player/currently_playing`;

        this.httpClient.get<PlaybackState>(uri).subscribe(result => {
            this.currentlyPlayingSubject.next(result);
        });
    }




}
