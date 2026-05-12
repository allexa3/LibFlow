import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';

type Step = 'request' | 'verify' | 'done';

function passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
  const newPassword = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (newPassword && confirmPassword && newPassword !== confirmPassword) {
    return { passwordMismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-forgot-password',
  imports: [
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    RouterLink,
  ],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ForgotPasswordComponent {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly http = inject(HttpClient);

  protected readonly step = signal<Step>('request');
  protected readonly isSubmitting = signal(false);
  protected readonly errorMessage = signal<string | null>(null);
  protected readonly submittedEmail = signal('');
  protected readonly showNewPassword = signal(false);
  protected readonly showConfirmPassword = signal(false);

  /** Step 1: collect email + new password */
  protected readonly requestForm = this.fb.group(
    {
      email: ['', [Validators.required, Validators.email]],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordMatchValidator },
  );

  /** Step 2: collect the 6-digit code */
  protected readonly verifyForm = this.fb.group({
    code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
  });

  protected toggleNewPassword(): void {
    this.showNewPassword.update((v) => !v);
  }

  protected toggleConfirmPassword(): void {
    this.showConfirmPassword.update((v) => !v);
  }

  /** Step 1 submit: POST /password/forgot */
  protected submitRequest(): void {
    if (this.requestForm.invalid || this.isSubmitting()) {
      this.requestForm.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    const { email, newPassword, confirmPassword } = this.requestForm.getRawValue();

    this.http
      .post<{ message: string }>('http://localhost:8080/password/forgot', {
        email,
        newPassword,
        confirmPassword,
      })
      .subscribe({
        next: () => {
          this.submittedEmail.set(email);
          this.isSubmitting.set(false);
          this.step.set('verify');
        },
        error: (err) => {
          this.isSubmitting.set(false);
          const body = err?.error;
          this.errorMessage.set(
            body?.error ?? body?.message ?? 'Failed to send reset code. Please try again.',
          );
        },
      });
  }

  protected submitVerify(): void {
    if (this.verifyForm.invalid || this.isSubmitting()) {
      this.verifyForm.markAllAsTouched();
      return;
    }

    this.errorMessage.set(null);
    this.isSubmitting.set(true);

    const { code } = this.verifyForm.getRawValue();
    const { email, newPassword, confirmPassword } = this.requestForm.getRawValue();

    this.http
      .post<{ message: string }>('http://localhost:8080/password/reset', {
        email,
        code,
        newPassword,
        confirmPassword,
      })
      .subscribe({
        next: () => {
          this.isSubmitting.set(false);
          this.step.set('done');
        },
        error: (err) => {
          this.isSubmitting.set(false);
          const body = err?.error;
          this.errorMessage.set(
            body?.error ?? body?.message ?? 'Invalid or expired reset code. Please try again.',
          );
        },
      });
  }

  protected goBack(): void {
    this.errorMessage.set(null);
    this.step.set('request');
  }
}