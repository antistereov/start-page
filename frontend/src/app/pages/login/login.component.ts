import {Component, inject} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {RouterModule} from '@angular/router';
import {AuthService} from '../../auth/auth.service';
import {Router} from '@angular/router';
import {LoginFormComponent} from './login-form/login-form.component';

@Component({
  selector: 'app-login',
  standalone: true,
    imports: [ReactiveFormsModule, RouterModule, LoginFormComponent],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
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
                        this.router.navigate(['/']).then();
                    }
                    console.log(data);
                })
        }
    }
}
