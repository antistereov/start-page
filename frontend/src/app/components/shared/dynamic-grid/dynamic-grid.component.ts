import {AfterViewInit, Component, ComponentRef, ElementRef, Input, ViewChild, ViewContainerRef} from '@angular/core';
import {NgClass, NgForOf, NgStyle, NgTemplateOutlet} from '@angular/common';
import {CardModule} from 'primeng/card';
import {TileComponent} from '../tile/tile.component';
import {ScrollPanelModule} from 'primeng/scrollpanel';
import {Tile} from '../../tiles/tile.model';
import {TileRegistry} from '../../tiles/tile.registry';

@Component({
  selector: 'app-dynamic-grid',
  standalone: true,
    imports: [
        NgStyle,
        CardModule,
        NgForOf,
        NgClass,
        TileComponent,
        NgTemplateOutlet,
        ScrollPanelModule
    ],
  templateUrl: './dynamic-grid.component.html',
  styleUrl: './dynamic-grid.component.css'
})
export class DynamicGridComponent implements AfterViewInit {
    @Input() tiles: { type: string; properties: Tile }[] = [];
    @Input() direction: 'rows' | 'columns' = 'rows';
    @Input() size = 3;
    @Input() columnWidth = 200;
    @Input() rowHeight = 200;

    @ViewChild('gridContainer', { read: ViewContainerRef, static: true })
    container!: ViewContainerRef;

    @ViewChild('scrollContainer') scrollContainer!: ElementRef;

    ngAfterViewInit() {
        this.renderTiles();
    }

    renderTiles() {
        this.tiles.forEach((tileConfig) => {
            const tileComponent: any = TileRegistry[tileConfig.type];
            if (tileComponent) {
                const componentRef: ComponentRef<Tile> = this.container.createComponent(tileComponent);
                componentRef.instance.data = tileConfig.properties || {};
            }
        })
    }

    positions: { left: string, top: string, width: string, height: string }[] = [];

    constructor() {
        this.calculatePositions();
    }

    toggleTile(tile: Tile) {
        tile.toggle();
        this.calculatePositions();
    }

    private calculatePositions() {
        const occupied: boolean[][] = [];

        this.positions = this.tiles.map((tile) => {
            const width = this.direction == 'columns' ? tile.properties.width : tile.properties.height;
            const height = this.direction == 'columns' ? tile.properties.height : tile.properties.width;

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
