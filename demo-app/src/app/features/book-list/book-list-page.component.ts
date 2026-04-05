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
  protected readonly displayedColumns = ['title', 'authorName', 'isbn', 'actions'];

  constructor() {
    this.store.load();
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) {
      return;
    }

    this.dialog
      .open<BookFormDialogComponent, BookFormDialogData, BookFormDialogResult>(
        BookFormDialogComponent,
        { data: { title: 'Create Book', submitLabel: 'Create' } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((result) => {
        if (!result) return;
        this.store.create(result);
      });
  }

  protected openDeleteDialog(book: Book): void {
    if (this.isLoading()) {
      return;
    }

    if (confirm(`Delete book "${book.title}"?`)) {
      this.store.remove(book.id);
    }
  }
}