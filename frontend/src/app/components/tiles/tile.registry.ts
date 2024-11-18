import {SpotifyPlaybackTileComponent} from './spotify/spotify-playback-tile/spotify-playback-tile.component';
import {BaseTileComponent} from './base-tile/base-tile.component';
import {Type} from '@angular/core';

export const TileRegistry: Record<string, Type<any>> = {
    baseTile: BaseTileComponent,
    spotifyPlaybackTile: SpotifyPlaybackTileComponent,
}
