import {EventEmitter} from '@angular/core';

export class Tile<T = any> {
    width: number;
    height: number;
    data: T;
    tileClick: EventEmitter<Tile>;

    constructor(
        public name: string,
        public expanded: boolean,
        protected collapsedWidth: 1 | 2 | 3,
        protected collapsedHeight: 1 | 2 | 3,
        protected expandedWidth: 1 | 2 | 3,
        protected expandedHeight: 1 | 2 | 3,
        data: T
    ) {
        this.tileClick = new EventEmitter<Tile>;
        this.data = data;
        this.width = this.expanded ? expandedWidth : collapsedWidth;
        this.height = this.expanded ? expandedHeight : collapsedHeight;
    }

    toggle() {
        this.expanded = !this.expanded;

        this.width = this.expanded ? this.expandedWidth : this.collapsedWidth;
        this.height = this.expanded ? this.expandedHeight : this.collapsedHeight;
    }

    click() {
        this.tileClick.emit(this);
    }
}
