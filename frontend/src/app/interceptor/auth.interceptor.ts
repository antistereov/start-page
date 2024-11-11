import {HttpInterceptorFn} from '@angular/common/http';
import {inject} from '@angular/core';
import {AuthService} from '../auth/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const accessToken = authService.getAccessToken();

    if (accessToken) {
        const authReq = req.clone({
            setHeaders: { Authorization: `Bearer ${accessToken}` }
        });
        return next(authReq);
    }

    return next(req);
}
