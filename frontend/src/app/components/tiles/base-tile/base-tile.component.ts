import {Component, Input} from '@angular/core';

import {Tile} from '../tile.model';

@Component({
  selector: 'app-base-tile',
  standalone: true,
  imports: [],
  templateUrl: './base-tile.component.html',
  styleUrl: './base-tile.component.css'
})
export class BaseTileComponent extends Tile {
    constructor(
        name: string,
        expanded: boolean,
        collapsedWidth: 1 | 2 | 3,
        collapsedHeight: 1 | 2 | 3,
        expandedWidth: 1 | 2 | 3,
        expandedHeight: 1 | 2 | 3,
        data: any = {}
    ) {
        super(name, expanded, collapsedWidth, collapsedHeight, expandedWidth, expandedHeight, data);
    }
}
