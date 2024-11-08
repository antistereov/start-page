import { Component } from '@angular/core';
import {AccentColorSelectorComponent} from "../../theme/accent-color-selector/accent-color-selector.component";
import {DividerModule} from "primeng/divider";
import {ThemeSelectorComponent} from "../../theme/theme-selector/theme-selector.component";
import {PresetSelectorComponent} from '../../theme/preset-selector/preset-selector.component';

@Component({
  selector: 'app-appearance-settings',
  standalone: true,
    imports: [
        AccentColorSelectorComponent,
        DividerModule,
        ThemeSelectorComponent,
        PresetSelectorComponent
    ],
  templateUrl: './appearance-settings.component.html',
  styleUrl: './appearance-settings.component.css'
})
export class AppearanceSettingsComponent {

}
