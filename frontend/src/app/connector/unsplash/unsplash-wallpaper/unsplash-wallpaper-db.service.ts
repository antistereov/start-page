import Dexie, { Table } from 'dexie';

export class UnsplashWallpaperDatabase extends Dexie {
    wallpaperIds!: Table<UnsplashWallpaper, number>;

    constructor() {
        super('WallpaperDatabase');
        this.version(1).stores({
            wallpaperIds: '++id, dateAdded'
        });
    }
}

export interface UnsplashWallpaper {
    id?: number;
    dateAdded: Date;
    photoId: string;
}

export const db = new UnsplashWallpaperDatabase();
