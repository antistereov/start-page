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
import {CardModule} from 'primeng/card';
import {ProgressSpinnerModule} from 'primeng/progressspinner';

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
        NgIf,
        CardModule,
        ProgressSpinnerModule
    ],
  templateUrl: './login-form.component.html',
  styleUrl: './login-form.component.scss'
})
export class LoginFormComponent {
    protected form: FormGroup;
    protected loading: boolean = false;

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
            this.loading = true;
            this.authService.login(this.form.value)
                .subscribe({
                    next: (data: any) => {
                        this.router.navigate(['/']).then();
                        this.loading = false;
                    },
                    error: () => {
                        this.form.setErrors({'invalid': true});
                        this.loading = false;
                    }
                })
        }
    }
}
