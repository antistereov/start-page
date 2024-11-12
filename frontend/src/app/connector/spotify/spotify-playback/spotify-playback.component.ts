import {Component, OnInit} from '@angular/core';
import {withDebugTracing} from '@angular/router';
import {SpotifyPlaybackService} from './spotify-playback.service';
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'app-spotify-playback',
  standalone: true,
    imports: [
        ButtonModule
    ],
  templateUrl: './spotify-playback.component.html',
  styleUrl: './spotify-playback.component.css'
})
export class SpotifyPlaybackComponent {
    constructor(private spotifyService: SpotifyPlaybackService) {}

    async togglePlay() {
        await this.spotifyService.togglePlay();
    }

}
