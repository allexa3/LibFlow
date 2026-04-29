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
import { MatSelectModule } from '@angular/material/select';
import { MatOptionModule } from '@angular/material/core';
import { PersonService } from '../../services/person.service';
import { Person } from '../../models/person.model';
import { GenreService } from '../../services/genre.service';
import { Genre } from '../../models/genre.model';

export interface BookFormValue {
  title: string;
  authorName: string;
  isbn: string;
  personId: string | null;
  genreIds: string[];
}

export interface BookFormDialogData {
  title: string;
  submitLabel?: string;
  initialValue?: any | null;
}
export type BookFormDialogResult = BookFormValue | undefined;

@Component({
  selector: 'app-book-form-dialog',
  imports: [
    ReactiveFormsModule,
    MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSelectModule,
    MatOptionModule,
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
  protected people: Person[] = [];
  private readonly genreService = inject(GenreService);
  protected genres: Genre[] = [];

  protected readonly form = this.fb.nonNullable.group({
    title: ['', [Validators.required, Validators.minLength(2)]],
    authorName: ['', [Validators.required]],
    isbn: ['', [Validators.required, Validators.pattern(/^(978|979)[0-9]{10}$/)]],
    // personId is optional — empty string means "no borrower"
    personId: [''],
    genreIds: [[] as string[], [Validators.required]],
  });

  ngOnInit(): void {
    this.personService.getAll().subscribe((data) => (this.people = data));
    this.genreService.getAll().subscribe((data) => (this.genres = data));

    if (this.data.initialValue) {
      this.form.patchValue({
        ...this.data.initialValue,
        personId: this.data.initialValue.borrowedBy?.id ?? '',
        genreIds: this.data.initialValue.genres?.map((g: any) => g.id) ?? [],
      });
    }
  }

  protected submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    const raw = this.form.getRawValue();
    const result: BookFormValue = {
      ...raw,
      personId: raw.personId || null,
    };
    this.dialogRef.close(result);
  }

  protected cancel(): void {
    this.dialogRef.close(undefined);
  }
}