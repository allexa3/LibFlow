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
import { MatSelectModule } from '@angular/material/select'; // Required for mat-select
import { MatOptionModule } from '@angular/material/core';   // Required for mat-option
import { PersonService } from '../../services/person.service';
import { Person } from '../../models/person.model';

export interface BookFormDialogData {
  title: string;
  submitLabel?: string;
  initialValue?: BookFormValue | null;
}

export interface BookFormValue {
  title: string;
  authorName: string;
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
    MatSelectModule, // Add this
    MatOptionModule, // Add this
  ],
  templateUrl: './book-form-dialog.component.html',
  styleUrl: './book-form-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})

export class BookFormDialogComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly dialogRef = inject(MatDialogRef<BookFormDialogComponent>);
  protected readonly data = inject<BookFormDialogData>(MAT_DIALOG_DATA);
  private readonly personService = inject(PersonService);
  protected people: Person[] = []; // Store list of people for the dropdown

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(2)]],
    authorName: ['', [Validators.required]],
    isbn: ['', [Validators.required, Validators.pattern(/^(978|979)[0-9]{10}$/)]],
    personId: ['', [Validators.required]] // Matches Java DTO
  });

  ngOnInit(): void {
    // Fetch people so we can link a book to a person (1:n)
    this.personService.getAll().subscribe(data => this.people = data);
    
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


