import {Component} from '@angular/core';
import {NgClass, NgForOf, NgStyle, NgTemplateOutlet} from '@angular/common';
import {CardModule} from 'primeng/card';
import {TileComponent} from './tile/tile.component';
import {ScrollPanelModule} from 'primeng/scrollpanel';

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
export class DynamicGridComponent {
    columns = 3;
    columnWidth = 200;
    rowHeight = 200;

    tiles: Tile[] = [
        new Tile("first", false, 1, 1, 2, 1),
        new Tile("second", false, 1, 1, 2, 3),
        new Tile("third", false, 1, 1, 2, 2),
        new Tile("forth", false, 1, 1, 1, 2),
        new Tile("fifth", false, 1, 1, 3, 3),
        new Tile("sixth", false, 1, 1, 2, 2),
        new Tile("seventh", false, 1, 1, 1, 2),
    ];

    positions: { left: string, top: string, width: string, height: string }[] = [];

    constructor() {
        this.calculatePositions();
    }

    toggleTile(tile: Tile) {
        tile.toggle();
        this.calculatePositions();
    }

    private calculatePositions() {
        const occupied: boolean[][] = []; // Array of rows, each row is an array of columns
        let currentRow = 0;

        if (currentRow >= 10) return;

        this.positions = this.tiles.map((tile) => {
            let positionFound = false;
            let startRow = 0, startCol = 0;

            // Loop through rows, expanding rows as needed
            while (!positionFound) {
                // Ensure the current row exists in the occupied map
                if (!occupied[currentRow]) {
                    occupied[currentRow] = Array(this.columns).fill(false);
                }

                // Search for a position in the current row
                for (let col = 0; col < this.columns; col++) {
                    if (this.canPlaceTile(occupied, currentRow, col, tile.spanCols, tile.spanRows)) {
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
            for (let r = 0; r < tile.spanRows; r++) {
                // Ensure the row exists
                if (!occupied[startRow + r]) {
                    occupied[startRow + r] = Array(this.columns).fill(false);
                }
                for (let c = 0; c < tile.spanCols; c++) {
                    occupied[startRow + r]![startCol + c] = true;
                }
            }

            // Set the position for this tile
            return {
                left: `${startCol * this.columnWidth}px`,
                top: `${startRow * this.rowHeight}px`,
                width: `${tile.spanCols * this.columnWidth}px`,
                height: `${tile.spanRows * this.rowHeight}px`
            };
        });
    }

    private canPlaceTile(occupied: boolean[][], row: number, col: number, spanCols: number, spanRows: number): boolean {
        // Ensure the tile fits within the column boundaries
        if (col + spanCols > this.columns) {
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

export class Tile{
    spanCols: number;
    spanRows: number;

    constructor(
        public name: string,
        public expanded: boolean,
        protected collapsedCols: number,
        protected collapsedRows: number,
        protected expandedCols: number,
        protected expandedRows: number,
    ) {
        this.spanCols = this.expanded ? expandedCols : collapsedCols;
        this.spanRows = this.expanded ? expandedRows : collapsedRows;
    }

    toggle() {
        this.expanded = !this.expanded;

        this.spanCols = this.expanded ? this.expandedCols : this.collapsedCols;
        this.spanRows = this.expanded ? this.expandedRows : this.collapsedRows;
    }
}
