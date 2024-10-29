import {Component,} from '@angular/core';
import {InputGroupModule} from 'primeng/inputgroup';
import {InputGroupAddonModule} from 'primeng/inputgroupaddon';
import {InputTextModule} from 'primeng/inputtext';
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {PasswordModule} from 'primeng/password';
import {AuthService} from '../../../auth/auth.service';
import {Router} from '@angular/router';
import {ButtonModule} from 'primeng/button';
import {NgClass, NgIf} from '@angular/common';

@Component({
  selector: 'app-login-form',
  standalone: true,
    imports: [
        InputGroupModule,
        InputGroupAddonModule,
        InputTextModule,
        FormsModule,
        PasswordModule,
        ReactiveFormsModule,
        ButtonModule,
        NgClass,
        NgIf
    ],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.scss'
})
export class LoginFormComponent {
    protected form: FormGroup;

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router,
    ) {
        this.form = this.fb.group({
            username: ['', Validators.required],
            password: ['', Validators.required],
        })
    }

    onSubmit() {
        if (this.form.valid) {
            console.log(this.form.value);
            this.authService.login(this.form.value)
                .subscribe({
                    next: (data: any) => {
                        this.router.navigate(['/admin']).then();
                        console.log(data);
                    },
                    error: () => {
                        this.form.setErrors({'invalid': true});
                    }
                })
        }
    }
}
