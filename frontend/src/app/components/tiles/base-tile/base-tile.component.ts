import {Component, EventEmitter} from '@angular/core';
import {Tile, TileConfig, TileSize} from '../tile.model';
import {CardModule} from 'primeng/card';

@Component({
  selector: 'app-base-tile',
  standalone: true,
    imports: [
        CardModule
    ],
  templateUrl: './base-tile.component.html',
  styleUrl: './base-tile.component.css'
})
export class BaseTileComponent extends Tile {
    override config: TileConfig = {
        name: "Tile",
        properties: {}
    }

    tileClick = new EventEmitter<Tile>;

    override size = new TileSize(false, 1, 1, 2, 2)

    click() {
        this.tileClick.emit(this);
    }

    constructor() {
        super()
    }
}
