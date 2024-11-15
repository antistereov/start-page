import {Full} from 'unsplash-js/dist/methods/photos/types';

export interface UnsplashPhoto {
    id: string;
    urls: {
        full: string;
        raw: string;
        regular: string;
        small: string;
        thumb: string;
    };
    links: {
        self: string;
        html: string;
        download: string;
        download_location: string;
    };
    user: User;
    likedByUser: boolean;
}

export interface User {
    name: string,
    link: string,
}

export interface FullWithLiked extends Full {
    liked_by_user: boolean
}

export function toUnsplashPhoto(fullWithLiked: FullWithLiked): UnsplashPhoto {
    const id = fullWithLiked.id;
    const urls = fullWithLiked.urls;
    const user = { name: fullWithLiked.user.name, link: fullWithLiked.user.links.html };
    const likedByUser = fullWithLiked.liked_by_user;

    return { id: id, urls: urls, links: fullWithLiked.links, user: user, likedByUser: likedByUser }
}
