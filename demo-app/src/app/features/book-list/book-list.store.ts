import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize, Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Book, CreateBookDto } from '../../models/book.model';
import { BookService } from '../../services/book.service';
import { NotificationService } from '../../services/notification.service';

@Injectable({ providedIn: 'root' })
export class BookListStore {
  private readonly bookService = inject(BookService);
  private readonly notify = inject(NotificationService);
  private readonly pendingRequests = signal(0);

  readonly books = signal<Book[]>([]);
  readonly hasError = signal<boolean>(false);
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest() {
    this.pendingRequests.update((c) => c + 1);
    this.hasError.set(false);
  }

  private endRequest() {
    this.pendingRequests.update((c) => Math.max(0, c - 1));
  }

  load(): void {
    this.beginRequest();
    this.bookService.getAll().subscribe({
      next: (data) => {
        this.books.set(data);
        this.endRequest();
      },
      error: (err: HttpErrorResponse) => {
        this.hasError.set(true);
        this.endRequest();
        this.notify.parseAndShowError(err, 'Failed to load books. Please try again.');
      },
    });
  }

  create(dto: CreateBookDto): void {
    this.beginRequest();
    const request$ = this.bookService.create(dto) as unknown as Observable<Book>;
    request$.pipe(finalize(() => this.endRequest())).subscribe({
      next: (created: Book) => {
        this.books.update((list) => [...list, created]);
        this.notify.success(`Book "${created.title}" was created successfully.`);
      },
      error: (err: HttpErrorResponse) => {
        if (err.status === 409) {
          this.notify.error(
            'A book with this ISBN already exists. Please use a unique ISBN.',
          );
        } else {
          this.notify.parseAndShowError(
            err,
            'Failed to create book. Please check your input and try again.',
          );
        }
      },
    });
  }

  remove(id: string): void {
    const book = this.books().find((b) => b.id === id);
    this.beginRequest();
    const request$ = this.bookService.delete(id) as unknown as Observable<void>;
    request$.pipe(finalize(() => this.endRequest())).subscribe({
      next: () => {
        this.books.update((list) => list.filter((b) => b.id !== id));
        this.notify.success(`Book "${book?.title ?? ''}" was deleted successfully.`);
      },
      error: (err: HttpErrorResponse) => {
        this.notify.parseAndShowError(err, 'Failed to delete book. Please try again.');
      },
    });
  }

  update(id: string, dto: Partial<CreateBookDto>): void {
    this.beginRequest();
    this.bookService
      .patch(id, dto)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.books.update((list) => list.map((b) => (b.id === id ? updated : b)));
          this.notify.success(`Book "${updated.title}" was updated successfully.`);
        },
        error: (err: HttpErrorResponse) => {
          if (err.status === 409) {
            this.notify.error(
              'Cannot update: a book with this ISBN already exists. Please use a different ISBN.',
            );
          } else {
            this.notify.parseAndShowError(
              err,
              'Failed to update book. Please check your input and try again.',
            );
          }
        },
      });
  }

  borrow(bookId: string): void {
    this.beginRequest();
    this.bookService
      .borrow(bookId)
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (updated) => {
          this.books.update((list) => list.map((b) => (b.id === bookId ? updated : b)));
          this.notify.success(`You have successfully borrowed "${updated.title}".`);
        },
        error: (err: HttpErrorResponse) => {
          // 400 with our custom ValidationException message
          const body = err.error as { error?: string } | null;
          if (body?.error) {
            this.notify.error(body.error);
          } else {
            this.notify.parseAndShowError(err, 'Failed to borrow book. Please try again.');
          }
        },
      });
  }
}