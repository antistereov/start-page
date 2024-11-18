import {Component, Input} from '@angular/core';

import {Tile} from '../../tile.model';

@Component({
  selector: 'app-spotify-playback-tile',
  standalone: true,
  imports: [],
  templateUrl: './spotify-playback-tile.component.html',
  styleUrl: './spotify-playback-tile.component.css'
})
export class SpotifyPlaybackTileComponent extends Tile {
    @Input() set tileData(data: Partial<Tile>) {
        Object.assign(this, data);
    }

}
