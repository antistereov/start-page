import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';
import {Preset} from './preset.enum';
import {usePreset} from 'primeng/themes';
import {Aura} from 'primeng/themes/aura';
import {Lara} from 'primeng/themes/lara';
import {Nora} from 'primeng/themes/nora';
import {AccentColorSelectorService} from '../accent-color-selector/accent-color-selector.service';

@Injectable({
    providedIn: 'root'
})
export class PresetSelectorService {
    private presetSubject = new BehaviorSubject<Preset>(this.getInitialPreset());
    preset$ = this.presetSubject.asObservable();

    constructor(private accentColorService: AccentColorSelectorService) {
        this.applyPreset(this.presetSubject.value);
    }

    private getInitialPreset(): Preset {
        return (localStorage.getItem('preset') as Preset) || Preset.Aura;
    }

    setPreset(preset: Preset) {
        this.presetSubject.next(preset);
        localStorage.setItem('preset', preset);

        this.applyPreset(preset);
    }

    private applyPreset(preset: Preset) {
        switch (preset) {
            case Preset.Aura:
                usePreset(Aura);
                break;
            case Preset.Lara:
                usePreset(Lara);
                break;
            case Preset.Nora:
                usePreset(Nora)
                break;
        }
        this.accentColorService.setCurrentAccentColor()
    }
}
