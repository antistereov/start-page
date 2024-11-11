import {Component, OnInit} from '@angular/core';
import {AccentColorSelectorService} from './accent-color-selector.service';
import {AccentColor} from './accent-color.enum';
import {FormsModule} from '@angular/forms';
import {Select} from 'primeng/select';
import {ColorPickerModule} from 'primeng/colorpicker';
import {ButtonModule} from 'primeng/button';
import {SelectorComponentComponent} from './selector-component/selector-component.component';
import {NgForOf} from '@angular/common';
import {CardModule} from 'primeng/card';

@Component({
  selector: 'app-accent-color-selector',
  standalone: true,
    imports: [
        FormsModule,
        Select,
        ColorPickerModule,
        ButtonModule,
        SelectorComponentComponent,
        NgForOf,
        CardModule
    ],
  templateUrl: './accent-color-selector.component.html',
  styleUrl: './accent-color-selector.component.css'
})
export class AccentColorSelectorComponent implements OnInit {
    accentColorOptions = [
        { label: 'Emerald', value: AccentColor.Emerald },
        { label: 'Green', value: AccentColor.Green },
        { label: 'Lime', value: AccentColor.Lime },
        { label: 'Red', value: AccentColor.Red },
        { label: 'Orange', value: AccentColor.Orange },
        { label: 'Amber', value: AccentColor.Amber },
        { label: 'Yellow', value: AccentColor.Yellow },
        { label: 'Teal', value: AccentColor.Teal },
        { label: 'Cyan', value: AccentColor.Cyan },
        { label: 'Sky', value: AccentColor.Sky },
        { label: 'Blue', value: AccentColor.Blue },
        { label: 'Indigo', value: AccentColor.Indigo },
        { label: 'Violet', value: AccentColor.Violet },
        { label: 'Purple', value: AccentColor.Purple },
        { label: 'Fuchsia', value: AccentColor.Fuchsia },
        { label: 'Pink', value: AccentColor.Pink },
        { label: 'Rose', value: AccentColor.Rose }
    ]
    selectedAccentColor: AccentColor = AccentColor.Indigo;

    constructor(protected accentColorSelectorService: AccentColorSelectorService) {}

    ngOnInit() {
        this.accentColorSelectorService.accentColor$
            .subscribe((accentColor) => {
                this.selectedAccentColor = accentColor;
            });

    }


    onAccentColorChange(event: any) {
        this.accentColorSelectorService.setAccentColor(event.value as AccentColor);
    }

}
