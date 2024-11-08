import {Component, Input} from '@angular/core';
import {AccentColor} from '../accent-color.enum';
import {NgClass} from '@angular/common';
import {ButtonModule} from 'primeng/button';

@Component({
  selector: 'app-accent-color-selector-component',
  standalone: true,
    imports: [
        NgClass,
        ButtonModule
    ],
  templateUrl: './selector-component.component.html',
  styleUrl: './selector-component.component.css'
})
export class SelectorComponentComponent {
    @Input() color: string = AccentColor.Indigo;
    @Input() selected: boolean = false;

    toggle() {
        this.selected = !this.selected;
    }
}
