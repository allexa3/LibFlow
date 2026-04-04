import {
  ChangeDetectionStrategy,
  Component,
  OnInit,
  inject,
} from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

export interface BookFormDialogData {
  title: string;
  submitLabel?: string;
  initialValue?: BookFormValue | null;
}

export interface BookFormValue {
  title: string;
  isbn: string;
}

export type BookFormDialogResult = BookFormValue | undefined;

@Component({
  selector: 'app-book-form-dialog',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
  ],
  templateUrl: './book-form-dialog.component.html',
  styleUrl: './book-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<BookFormDialogComponent>);
  protected readonly data = inject<BookFormDialogData>(MAT_DIALOG_DATA);

  // Form group updated for Book fields: Title and ISBN [cite: 10, 25]
  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(2)]],
    isbn: ['', [Validators.required, Validators.minLength(10), Validators.maxLength(13)]],
  });

  ngOnInit(): void {
    if (this.data.initialValue) {
      this.form.patchValue(this.data.initialValue);
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const result: BookFormValue = this.form.getRawValue();
    this.dialogRef.close(result);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}