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
import { MatOptionModule } from '@angular/material/core';   // Required for mat-option
import { PersonService } from '../../services/person.service';
import { Person } from '../../models/person.model';
import { GenreService } from '../../services/genre.service'; // Ensure this exists
import { Genre } from '../../models/genre.model';

export interface BookFormValue {
  title: string;
  authorName: string;
  isbn: string;
  personId: string; // Add this
  genreIds: string[];
}

// Change initialValue to 'any' or your 'Book' model to allow .genres access
export interface BookFormDialogData {
  title: string;
  submitLabel?: string;
  initialValue?: any | null; 
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
  private readonly genreService = inject(GenreService);
  protected genres: Genre[] = []; // List for the multi-select dropdown

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(2)]],
    authorName: ['', [Validators.required]],
    isbn: ['', [Validators.required, Validators.pattern(/^(978|979)[0-9]{10}$/)]],
    personId: ['', [Validators.required]],
    genreIds: [[] as string[], [Validators.required]] // Added for n:m relationship
  });

  ngOnInit(): void {
    // Fetch both People and Genres
    this.personService.getAll().subscribe(data => this.people = data);
    this.genreService.getAll().subscribe(data => this.genres = data);

    if (this.data.initialValue) {
      this.form.patchValue({
        ...this.data.initialValue,
        // Map existing genres back to IDs for the form
        genreIds: this.data.initialValue.genres?.map((g: any) => g.id) || []
      });
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