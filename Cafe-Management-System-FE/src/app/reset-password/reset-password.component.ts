import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { UserService } from '../services/user.service';
import { NgxUiLoaderService } from 'ngx-ui-loader';
import { SnackbarService } from '../services/snackbar.service';
import { ActivatedRoute } from '@angular/router';
import { GlobalConstants } from '../shared/global-constants';

@Component({
  selector: 'app-reset-password',
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.scss']
})
export class ResetPasswordComponent implements OnInit {

  passwordVisible = false;
  confirmPasswordVisible = false;

  resetPasswordForm!: FormGroup;
  token: string | null = '';

  responseMessage: string = '';
  isResetSuccessful = false;   // ✅ controls UI state

  constructor(
    private formBuilder: FormBuilder,
    private userService: UserService,
    private ngxService: NgxUiLoaderService,
    private snackbarService: SnackbarService,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {

    this.token = this.route.snapshot.queryParamMap.get('token');

    this.resetPasswordForm = this.formBuilder.group({
      password: [null, [
        Validators.required,
        Validators.pattern(GlobalConstants.passwordRegex)
      ]],
      confirmPassword: [null, Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirmPassword = form.get('confirmPassword')?.value;

    if (password !== confirmPassword) {
      form.get('confirmPassword')?.setErrors({ mismatch: true });
    } else {
      form.get('confirmPassword')?.setErrors(null);
    }
  }

  handleSubmit() {

    if (this.resetPasswordForm.invalid) {
      return;
    }

    this.ngxService.start();

    const data = {
      token: this.token,
      newPassword: this.resetPasswordForm.value.password,
    };

    this.userService.resetPassword(data).subscribe(
      (response: any) => {

        this.ngxService.stop();

        this.responseMessage = response?.message || 'Password reset successfully!';
        this.isResetSuccessful = true;   // ✅ hide form, show success view
      },
      (error) => {

        this.ngxService.stop();

        this.responseMessage =
          error.error?.message || GlobalConstants.genericError;

        this.snackbarService.openSnackBar(
          this.responseMessage,
          GlobalConstants.error
        );
      }
    );
  }
}