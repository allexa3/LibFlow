import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
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

type SortField = 'title' | 'title_desc' | 'author' | 'isbn';
type AvailabilityFilter = 'all' | 'available' | 'borrowed';

@Component({
  selector: 'app-book-list-page',
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatDialogModule,
    MatToolbarModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
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

  // Filter & sort state
  protected readonly searchQuery = signal('');
  protected readonly availabilityFilter = signal<AvailabilityFilter>('all');
  protected readonly genreFilter = signal<string>('all');
  protected readonly sortField = signal<SortField>('title');

  /** Unique genre names extracted from all loaded books */
  protected readonly uniqueGenres = computed(() => {
    const names = new Set<string>();
    for (const book of this.books()) {
      for (const g of book.genres ?? []) {
        names.add(g.name);
      }
    }
    return Array.from(names).sort();
  });

  protected readonly hasActiveFilters = computed(
    () =>
      this.searchQuery().trim() !== '' ||
      this.availabilityFilter() !== 'all' ||
      this.genreFilter() !== 'all',
  );

  /**
   * Filtering is done on the frontend.
   * Justification: The full book list is already fetched on page load. Performing
   * filtering client-side avoids additional HTTP round-trips on every keystroke or
   * dropdown change, which would add latency and unnecessary server load. Since the
   * dataset is small and fully in memory, frontend filtering is faster and provides
   * instant feedback to the user.
   */
  protected readonly filteredBooks = computed(() => {
    const query = this.searchQuery().trim().toLowerCase();
    const availability = this.availabilityFilter();
    const genre = this.genreFilter();
    const sort = this.sortField();

    let result = this.books().filter((book) => {
      // Filter criterion 1: search by title or author
      const matchesSearch =
        !query ||
        book.title.toLowerCase().includes(query) ||
        (book.authorName ?? '').toLowerCase().includes(query);

      // Filter criterion 2: availability status
      const matchesAvailability =
        availability === 'all' ||
        (availability === 'available' && !book.borrowedBy) ||
        (availability === 'borrowed' && !!book.borrowedBy);

      // Filter criterion 3: genre
      const matchesGenre =
        genre === 'all' || (book.genres ?? []).some((g) => g.name === genre);

      return matchesSearch && matchesAvailability && matchesGenre;
    });

    // Sort
    result = [...result].sort((a, b) => {
      switch (sort) {
        case 'title':
          return a.title.localeCompare(b.title);
        case 'title_desc':
          return b.title.localeCompare(a.title);
        case 'author':
          return (a.authorName ?? '').localeCompare(b.authorName ?? '');
        case 'isbn':
          return a.isbn.localeCompare(b.isbn);
        default:
          return 0;
      }
    });

    return result;
  });

  constructor() {
    this.store.load();
  }

  protected onSearchChange(event: Event): void {
    this.searchQuery.set((event.target as HTMLInputElement).value);
  }

  protected clearSearch(): void {
    this.searchQuery.set('');
  }

  protected onAvailabilityChange(value: AvailabilityFilter): void {
    this.availabilityFilter.set(value);
  }

  protected onGenreFilterChange(value: string): void {
    this.genreFilter.set(value);
  }

  protected onSortChange(value: SortField): void {
    this.sortField.set(value);
  }

  protected clearAllFilters(): void {
    this.searchQuery.set('');
    this.availabilityFilter.set('all');
    this.genreFilter.set('all');
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