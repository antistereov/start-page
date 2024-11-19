import {Directive, EventEmitter} from "@angular/core";

@Directive()
export abstract class Tile<T = any> {
    abstract config: TileConfig<T>;
    abstract size: TileSize;

    abstract tileClick: EventEmitter<Tile>;

    toggle() {
        this.size.toggle();
    }

}

export interface TileConfig<T = any> {
    name: string;
    properties: T;
}

export class TileSize {
    expanded: boolean;

    width: 1 | 2 | 3;
    height: 1 | 2 | 3;

    constructor(
        expanded: boolean = false,
        private collapsedWidth: 1 | 2 | 3 = 1,
        private collapsedHeight: 1 | 2 | 3 = 1,
        private expandedWidth: 1 | 2 | 3 = 2,
        private expandedHeight: 1 | 2 | 3 = 2,
    ) {
        this.expanded = expanded;

        this.width = this.expanded ? this.expandedWidth : this.collapsedWidth;
        this.height = this.expanded ? this.expandedHeight : this.collapsedHeight;
    }

    toggle() {
        this.expanded = !this.expanded;
        this.width = this.expanded ? this.expandedWidth : this.collapsedWidth;
        this.height = this.expanded ? this.expandedHeight : this.collapsedHeight;
    }
}
