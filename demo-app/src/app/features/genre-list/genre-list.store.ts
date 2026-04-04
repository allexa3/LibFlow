import { computed, inject, Injectable, signal } from '@angular/core';
import { finalize, Observable } from 'rxjs';
import { HttpErrorResponse } from '@angular/common/http';
import { Genre, CreateGenreDto } from '../../models/genre.model';
import { GenreService } from '../../services/genre.service';

@Injectable({ providedIn: 'root' })
export class GenreListStore {
  private readonly genreService = inject(GenreService);
  private readonly pendingRequests = signal(0);

  readonly genres = signal<Genre[]>([]);
  readonly isLoading = computed(() => this.pendingRequests() > 0);

  private beginRequest() { this.pendingRequests.update(c => c + 1); }
  private endRequest() { this.pendingRequests.update(c => Math.max(0, c - 1)); }

  load(): void {
    this.beginRequest();
    const request$ = this.genreService.getAll() as unknown as Observable<Genre[]>;
    request$
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (data) => this.genres.set(data),
        error: () => alert("Failed to load genres")
      });
  }

  create(dto: CreateGenreDto): void {
    this.beginRequest();
    const request$ = this.genreService.create(dto) as unknown as Observable<Genre>;
    request$
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: (created: Genre) => {
          this.genres.update((list) => [...list, created]);
        },
        error: (error: HttpErrorResponse) => {
          // Requirement: Validations visible in frontend 
          alert(error.error?.message || "Validation Error");
        }
      });
  }

  remove(id: string): void {
    this.beginRequest();
    const request$ = this.genreService.delete(id) as unknown as Observable<void>;
    request$
      .pipe(finalize(() => this.endRequest()))
      .subscribe({
        next: () => {
          this.genres.update((list) => list.filter(genre => genre.id !== id));
        },
        error: () => alert("Failed to delete genre")
      });
  }
}