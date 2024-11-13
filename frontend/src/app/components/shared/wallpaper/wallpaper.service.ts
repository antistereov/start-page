import {Injectable} from '@angular/core';
import {BehaviorSubject} from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class WallpaperService {
    private backgroundUrlSubject = new BehaviorSubject<string>('');
    backgroundUrl$ = this.backgroundUrlSubject.asObservable();

    setWallpaper(url: string) {
        this.backgroundUrlSubject.next(url);
    }

}
