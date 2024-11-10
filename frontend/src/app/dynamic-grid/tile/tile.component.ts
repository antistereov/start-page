import {Component, Input} from '@angular/core';
import {NgClass, NgStyle} from '@angular/common';
import {CardModule} from 'primeng/card';

@Component({
  selector: 'app-tile',
  standalone: true,
    imports: [
        NgStyle,
        CardModule,
        NgClass
    ],
  templateUrl: './tile.component.html',
  styleUrl: './tile.component.css',
})
export class TileComponent {
    @Input() isExpanded = false;
    @Input() expandedCols: 1 | 2 | 3 = 2;
    @Input() expandedRows: 1 | 2 | 3 = 2;


    toggleExpansion(): void {
        this.isExpanded = !this.isExpanded;
    }
}
