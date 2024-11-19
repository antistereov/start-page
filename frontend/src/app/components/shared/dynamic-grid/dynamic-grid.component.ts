import {AfterViewInit, Component, ComponentRef, Input, ViewChild, ViewContainerRef} from '@angular/core';
import {NgClass, NgForOf, NgStyle, NgTemplateOutlet} from '@angular/common';
import {CardModule} from 'primeng/card';
import {ScrollPanelModule} from 'primeng/scrollpanel';
import {TileRegistry} from '../../tiles/tile.registry';
import {Tile, TileConfig} from '../../tiles/tile.model';

@Component({
  selector: 'app-dynamic-grid',
  standalone: true,
    imports: [
        NgStyle,
        CardModule,
        NgForOf,
        NgClass,
        NgTemplateOutlet,
        ScrollPanelModule
    ],
  templateUrl: './dynamic-grid.component.html',
  styleUrl: './dynamic-grid.component.css'
})
export class DynamicGridComponent implements AfterViewInit {
    @Input() tiles: { type: string; config: TileConfig }[] = [
        { type: 'baseTile', config: { name: 'first', properties: {} } },
        { type: 'spotifyPlaybackTile', config: { name: 'second', properties: {} }},
        { type: 'baseTile', config: { name: 'third', properties: {} } }
    ];
    @Input() direction: 'rows' | 'columns' = 'rows';
    @Input() size = 3;
    @Input() columnWidth = 200;
    @Input() rowHeight = 200;

    @ViewChild('gridContainer', { read: ViewContainerRef, static: true })
    container!: ViewContainerRef;

    // TODO: Find way to combine those
    private componentRefs: ComponentRef<Tile>[] = []
    private positions: Position[] = []

    ngAfterViewInit() {
        this.renderTiles();
        this.calculatePositions();
        this.applyPositions();
    }

    renderTiles() {
        this.tiles.forEach((tileConfig) => {
            const tileComponent: any = TileRegistry[tileConfig.type];
            if (tileComponent) {
                const componentRef: ComponentRef<Tile> = this.container.createComponent(tileComponent);
                this.componentRefs.push(componentRef);

                const element = componentRef.location.nativeElement as HTMLElement;
                // TODO: Set styling in CSS
                element.style.transition = 'all 0.3s ease';
                element.style.padding = '4px';

                Object.assign(componentRef.instance.config, tileConfig.config);

                componentRef.instance.tileClick.subscribe(tile => {
                    this.toggleTile(tile);
                })
            }
        })
    }

    constructor() {
        this.calculatePositions();
        this.applyPositions();
    }

    toggleTile(tile: Tile) {
        tile.toggle();
        this.calculatePositions();
        this.applyPositions();
    }

    private applyPositions() {
        this.componentRefs.forEach((componentRef, index) => {
            const position = this.positions[index];
            if (position) {
                const element = componentRef.location.nativeElement as HTMLElement;
                element.style.position = 'absolute';
                element.style.left = position.left;
                element.style.top = position.top;
                element.style.width = position.width;
                element.style.height = position.height;
            }
        })
    }

    private calculatePositions() {
        const occupied: boolean[][] = [];

        this.positions = this.componentRefs.map((tile) => {
            const width = this.direction == 'columns' ? tile.instance.size.width : tile.instance.size.height;
            const height = this.direction == 'columns' ? tile.instance.size.height : tile.instance.size.width;

            let positionFound = false;
            let startRow = 0, startCol = 0;

            let currentRow = 0;

            // Loop through rows, expanding rows as needed
            while (!positionFound) {
                // Ensure the current row exists in the occupied map
                if (!occupied[currentRow]) {
                    occupied[currentRow] = Array(this.size).fill(false);
                }

                // Search for a position in the current row
                for (let col = 0; col < this.size; col++) {
                    if (this.canPlaceTile(occupied, currentRow, col, width, height)) {
                        startRow = currentRow;
                        startCol = col;
                        positionFound = true;
                        break;
                    }
                }

                // If no position found in this row, move to the next row
                if (!positionFound) {
                    currentRow++;
                }
            }

            // Mark the occupied cells for this tile
            for (let r = 0; r < height; r++) {
                // Ensure the row exists
                if (!occupied[startRow + r]) {
                    occupied[startRow + r] = Array(this.size).fill(false);
                }
                for (let c = 0; c < width; c++) {
                    occupied[startRow + r]![startCol + c] = true;
                }
            }

            // Set the position for this tile
            if (this.direction === 'columns') {
                return {
                    left: `${startCol * this.columnWidth}px`,
                    top: `${startRow * this.rowHeight}px`,
                    width: `${width * this.columnWidth}px`,
                    height: `${height * this.rowHeight}px`
                };
            } else {
                return {
                    left: `${startRow * this.rowHeight}px`,
                    top: `${startCol * this.columnWidth}px`,
                    width: `${height * this.rowHeight}px`,
                    height: `${width * this.columnWidth}px`
                };
            }
        });
    }

    private canPlaceTile(occupied: boolean[][], row: number, col: number, spanCols: number, spanRows: number): boolean {
        // Ensure the tile fits within the column boundaries
        if (col + spanCols > this.size) {
            return false;
        }

        // Check if all required cells for the tile are free
        for (let r = 0; r < spanRows; r++) {
            for (let c = 0; c < spanCols; c++) {
                if (occupied[row + r]?.[col + c]) {
                    return false;
                }
            }
        }
        return true;
    }

}

interface Position {
    left: string;
    top: string;
    width: string;
    height: string;
}
