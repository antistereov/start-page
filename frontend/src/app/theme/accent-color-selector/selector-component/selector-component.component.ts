import {Component, ElementRef, Input, OnInit, Renderer2} from '@angular/core';
import {AccentColor} from '../accent-color.enum';
import {NgClass} from '@angular/common';
import {ButtonModule} from 'primeng/button';
import {ThemeSelectorService} from '../../theme-selector/theme-selector.service';

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
export class SelectorComponentComponent implements OnInit {
    @Input() color: string = AccentColor.Indigo;
    @Input() selected: boolean = false;

    constructor(
        private themeService: ThemeSelectorService,
        private renderer: Renderer2,
        private el: ElementRef,
    ) {
    }

    toggle() {
        this.selected = !this.selected;
    }

    ngOnInit() {
        this.themeService.isDarkModeSubject$.subscribe(isDarkMode => {
            const colorSquare = this.el.nativeElement.querySelector('.color-square')
            if (isDarkMode) {
                this.renderer.addClass(colorSquare, 'dark-mode');
            } else {
                this.renderer.removeClass(colorSquare, 'dark-mode')
            }
        });
    }
}
