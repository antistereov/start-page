import {Component, EventEmitter} from '@angular/core';

import {Tile, TileConfig, TileSize} from '../../tile.model';
import {SpotifyPlaybackService} from '../../../../connector/spotify/spotify-web-api/spotify-playback.service';
import {CardModule} from 'primeng/card';
import {PlaybackState, Track} from '@spotify/web-api-ts-sdk';
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'app-spotify-playback-tile',
  standalone: true,
    imports: [
        CardModule,
        ButtonModule
    ],
  templateUrl: './spotify-playback-tile.component.html',
  styleUrl: './spotify-playback-tile.component.css'
})
export class SpotifyPlaybackTileComponent extends Tile {
    albumArt: string | null = null;

    override config: TileConfig = {
        name: "SpotifyPlaybackTile",
        properties: {}
    }

    tileClick = new EventEmitter<Tile>;

    override size = new TileSize(false, 1, 1, 2, 2)

    click() {
        this.tileClick.emit(this);
    }

    constructor(private spotifyPlaybackService: SpotifyPlaybackService) {
        super()
        this.spotifyPlaybackService.startPolling();

        this.spotifyPlaybackService.currentlyPlaying$.subscribe(currentlyPlaying => {
            if (currentlyPlaying) {
                this.setCurrentlyPlaying(currentlyPlaying)
            }
        })
    }

    private setCurrentlyPlaying(currentlyPlaying: PlaybackState) {
        const item = currentlyPlaying.item as Track;

        this.albumArt = item.album.images.at(0)?.url ?? null;
    }


    skipToNext(deviceId: string | null = null) {
        this.spotifyPlaybackService.skipToNext(deviceId).subscribe();
    }

    skipToPrevious(deviceId: string | null = null) {
        this.spotifyPlaybackService.skipToPrevious(deviceId).subscribe();
    }


}
