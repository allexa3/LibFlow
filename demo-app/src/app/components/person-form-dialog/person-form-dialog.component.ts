import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
  signal,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
// NEW IMPORTS FOR SELECT
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';

export interface PersonFormDialogData {
  title: string;
  submitLabel?: string;
  showPasswordField?: boolean;
  initialValue?: PersonFormInitialValue | null;
}

export interface PersonFormValue {
  name: string;
  age: number;
  email: string;
  role: string; // NEW FIELD 
  password?: string;
}

export interface PersonFormInitialValue {
  name: string;
  age: number;
  email: string;
  role: string; // NEW FIELD 
}

export type PersonFormDialogResult = PersonFormValue | undefined;

@Component({
  selector: 'app-person-form-dialog',
  standalone: true, // Ensure standalone is true [cite: 10]
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule, // ADDED [cite: 36]
    MatOptionModule, // ADDED [cite: 36]
  ],
  templateUrl: './person-form-dialog.component.html',
  styleUrl: './person-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PersonFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<PersonFormDialogComponent>);
  protected readonly data = inject<PersonFormDialogData>(MAT_DIALOG_DATA);

  protected readonly isPasswordVisible = signal(false);

  // UPDATED FORM GROUP [cite: 16, 32]
  protected readonly form = this.fb.nonNullable.group({
    name: ['', [Validators.required, Validators.minLength(2)]],
    age: [0, [Validators.required, Validators.min(18), Validators.max(200)]],
    email: ['', [Validators.required, Validators.email]],
    role: ['CUSTOMER', [Validators.required]], // NEW CONTROL WITH DEFAULT 
    password: ['', []],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      this.form.patchValue(this.data.initialValue);
    }

    if (this.data.showPasswordField) {
      this.form.controls.password.setValidators([Validators.required]);
      this.form.controls.password.updateValueAndValidity();
    }
  }

  protected togglePasswordVisibility(): void {
    this.isPasswordVisible.update((v) => !v);
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    // UPDATED SUBMIT LOGIC 
    const { name, age, email, role, password } = this.form.getRawValue();
    const result: PersonFormValue = this.data.showPasswordField
      ? { name, age, email, role, password }
      : { name, age, email, role };

    this.dialogRef.close(result);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}