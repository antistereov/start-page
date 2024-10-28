import {Component, inject} from '@angular/core';
import {AuthService} from '../../auth/auth.service';
import { Router } from '@angular/router';
import {HttpClient} from '@angular/common/http';
import {NgForOf, NgIf} from '@angular/common';

@Component({
  selector: 'app-admin',
  standalone: true,
    imports: [
        NgIf,
        NgForOf
    ],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})
export class AdminComponent {
    authService = inject(AuthService);
    router = inject(Router);
    httpClient = inject(HttpClient);

    userProfile: UserProfile | null = null;

    public logout() {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    public fetchUserProfile() {
        this.httpClient.get('http://localhost:8000/me').subscribe({
            next: (data: any) => {
                this.userProfile = data;
            },
            error: (error) => {
                console.error('Error fetching user profile:', error);
            }
        });
    }


}

interface UserProfile {
    username: string;
    roles: string[];
}
