import {Component, inject} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {AuthService} from '../../auth/auth.service';
import {Router} from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
    authService = inject(AuthService);
    router = inject(Router);

    protected loginForm = new FormGroup({
        username: new FormControl('', [Validators.required]),
        password: new FormControl('', [Validators.required])
    })

    onSubmit() {
        if (this.loginForm.valid) {
            console.log(this.loginForm.value);
            this.authService.login(this.loginForm.value)
                .subscribe((data: any) => {
                    if (this.authService.isLoggedIn()) {
                        this.router.navigate(['/admin']);
                    }
                    console.log(data);
                })
        }
    }
}
