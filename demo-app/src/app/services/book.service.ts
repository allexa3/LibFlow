import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book } from '../models/book.model';

@Injectable({ providedIn: 'root' })
export class BookService {
  create(arg0: { title: string; isbn: string; }) {
      throw new Error('Method not implemented.');
  }
  private readonly http = inject(HttpClient);
  private readonly url = 'http://localhost:8080/book';

  getAll(): Observable<Book[]> {
    return this.http.get<Book[]>(this.url);
  }

  // Example of using the PATCH method
  patch(id: string, updates: Partial<Book>): Observable<Book> {
    return this.http.patch<Book>(`${this.url}/${id}`, updates);
  }

  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.url}/${id}`);
  }
}