import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  inject,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive } from '@angular/router';
import {
  BookFormDialogComponent,
  BookFormDialogData,
  BookFormDialogResult,
} from '../../components/book-form-dialog/book-form-dialog.component';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component';
import { Book } from '../../models/book.model';
import { BookListStore } from './book-list.store';

@Component({
  selector: 'app-book-list-page',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatToolbarModule,
    RouterLink,
    RouterLinkActive,
  ],
  templateUrl: './book-list-page.component.html',
  styleUrl: './book-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BookListPageComponent {
  private readonly dialog = inject(MatDialog);
  private readonly store = inject(BookListStore);
  private readonly destroyRef = inject(DestroyRef);

  protected readonly books = this.store.books;
  protected readonly isLoading = this.store.isLoading;
  protected readonly hasError = this.store.hasError;
  protected readonly displayedColumns = ['title', 'authorName', 'isbn', 'genres', 'borrowedBy', 'actions'];

  constructor() {
    this.store.load();
  }

  protected openCreateDialog(): void {
    this.dialog
      .open<BookFormDialogComponent, BookFormDialogData, BookFormDialogResult>(
        BookFormDialogComponent,
        { data: { title: 'Create Book', submitLabel: 'Create' } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (result) this.store.create(result);
      });
  }

  protected openEditDialog(book: Book): void {
    this.dialog
      .open<BookFormDialogComponent, BookFormDialogData, BookFormDialogResult>(
        BookFormDialogComponent,
        { data: { title: 'Edit Book', submitLabel: 'Update', initialValue: book } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (result) this.store.update(book.id, result);
      });
  }

  protected openDeleteDialog(book: Book): void {
    this.dialog
      .open<ConfirmDeleteDialogComponent, { name: string }, boolean>(
        ConfirmDeleteDialogComponent,
        { data: { name: book.title } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((confirmed) => {
        if (confirmed) this.store.remove(book.id);
      });
  }
}