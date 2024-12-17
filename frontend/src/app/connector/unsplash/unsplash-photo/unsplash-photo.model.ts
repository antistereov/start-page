export interface UnsplashPhoto {
    id: string;
    url: string;
    link: string;
    user: User;
    color: string;
    liked_by_user: boolean;
}

export interface User {
    name: string,
    link: string,
}
