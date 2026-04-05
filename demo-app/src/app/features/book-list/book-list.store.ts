import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize, Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Book, CreateBookDto } from '../../models/book.model'; // Ensure BookCreateDto matches your model
import { BookService } from '../../services/book.service';

@Injectable({ providedIn: 'root' })
export class BookListStore {
  private readonly bookService = inject(BookService);
  private readonly pendingRequests = signal(0);
  
  // Create signals for data and error state
  readonly books = signal<Book[]>([]);
  readonly hasError = signal<boolean>(false); // Added missing signal
  readonly isLoading = computed(() => this.pendingRequests() > 0);
  

  private beginRequest() { 
    this.pendingRequests.update(c => c + 1); 
    this.hasError.set(false); // Reset error on new request
  }
  
  private endRequest() { 
    this.pendingRequests.update(c => Math.max(0, c - 1)); 
  }

 load(): void {

    this.beginRequest();

    const request$ = this.bookService.getAll() as unknown as Observable<Book[]>;

    request$

      .pipe(finalize(() => this.endRequest()))

      .subscribe({

        next: (data) => this.books.set(data),

        error: () => alert("Failed to load books")

      });

  }



  create(dto: CreateBookDto): void {

    this.beginRequest();

    const request$ = this.bookService.create(dto) as unknown as Observable<Book>;

    request$

      .pipe(finalize(() => this.endRequest()))

      .subscribe({

        next: (created: Book) => { // Explicitly type 'created'

          this.books.update((list) => [...list, created]);

        },

        error: (error: HttpErrorResponse) => {

          // Requirement 40: Validations visible in frontend

          alert(error.error?.message || "Validation Error");

        }

      });

  }



  remove(id: string): void {

    this.beginRequest();

    const request$ = this.bookService.delete(id) as unknown as Observable<void>;

    request$

      .pipe(finalize(() => this.endRequest()))

      .subscribe({

        next: () => {

          this.books.update((list) => list.filter(book => book.id !== id));

        },

        error: () => alert("Failed to delete book")

      });

  }

  update(id: string, dto: Partial<CreateBookDto>): void {
  this.beginRequest();
  this.bookService.patch(id, dto) // Calls @PatchMapping in Java
    .pipe(finalize(() => this.endRequest()))
    .subscribe({
      next: (updated) => {
        this.books.update(list => list.map(b => b.id === id ? updated : b));
      },
      error: (err) => alert(err.error?.message || "Update failed")
    });
}

}