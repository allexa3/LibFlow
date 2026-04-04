import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { GenreListStore } from './genre-list.store';
import { Genre } from '../../models/genre.model';

@Component({
  selector: 'app-genre-list-page',
  standalone: true,
  imports: [
    MatTableModule, 
    MatButtonModule, 
    MatIconModule, 
    MatToolbarModule,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './genre-list-page.component.html',
  styleUrl: './genre-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenreListPageComponent {
  protected readonly store = inject(GenreListStore);
  protected readonly genres = this.store.genres;
  protected readonly isLoading = this.store.isLoading;
  protected readonly displayedColumns = ['name', 'actions'];

  constructor() {
    this.store.load();
  }

  protected openCreateDialog(): void {
    const name = prompt("Enter Genre Name:");
    if (name) {
      this.store.create({ name });
    }
  }

  protected deleteGenre(genre: Genre): void {
    if (confirm(`Delete genre "${genre.name}"?`)) {
      this.store.remove(genre.id);
    }
  }
}