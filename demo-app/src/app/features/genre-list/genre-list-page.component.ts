import { ChangeDetectionStrategy, Component, DestroyRef, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog'; //
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatToolbarModule } from '@angular/material/toolbar';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { ConfirmDeleteDialogComponent } from '../../components/confirm-delete-dialog/confirm-delete-dialog.component'; //
import {
  GenreFormDialogComponent,
  GenreFormDialogData,
  GenreFormDialogResult,
} from '../../components/genre-form-dialog/genre-form-dialog.component';
import { Genre } from '../../models/genre.model';
import { GenreListStore } from './genre-list.store';

@Component({
  selector: 'app-genre-list-page',
  standalone: true,
  imports: [
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatDialogModule, //
    RouterLink,
    RouterLinkActive,
  ],
  templateUrl: './genre-list-page.component.html',
  styleUrl: './genre-list-page.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GenreListPageComponent {
  private readonly dialog = inject(MatDialog); //
  private readonly store = inject(GenreListStore);
  private readonly destroyRef = inject(DestroyRef); //

  protected readonly genres = this.store.genres;
  protected readonly isLoading = this.store.isLoading;
  protected readonly displayedColumns = ['name', 'actions'];

  constructor() {
    this.store.load();
  }

  protected openCreateDialog(): void {
    if (this.isLoading()) return;

    // Use MatDialog instead of prompt()
    this.dialog
      .open<GenreFormDialogComponent, GenreFormDialogData, GenreFormDialogResult>(
        GenreFormDialogComponent,
        { data: { title: 'Create Genre', submitLabel: 'Create' } },
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef)) //
      .subscribe((result) => {
        if (result) {
          this.store.create(result);
        }
      });
  }

  protected deleteGenre(genre: Genre): void {
    if (this.isLoading()) return;

    // Use MatDialog instead of confirm()
    this.dialog
      .open<ConfirmDeleteDialogComponent, { genre: Genre }, boolean>(
        ConfirmDeleteDialogComponent,
        { data: { genre } }, // Pass genre data to the dialog
      )
      .afterClosed()
      .pipe(takeUntilDestroyed(this.destroyRef)) //
      .subscribe((confirmed) => {
        if (confirmed) {
          this.store.remove(genre.id);
        }
      });
  }

  protected editGenre(genre: Genre): void {
  this.dialog
    .open<GenreFormDialogComponent, GenreFormDialogData, GenreFormDialogResult>(
      GenreFormDialogComponent,
      { data: { title: 'Edit Genre', submitLabel: 'Update', initialValue: { name: genre.name } } }
    )
    .afterClosed()
    .pipe(takeUntilDestroyed(this.destroyRef))
    .subscribe((result) => {
      if (result) this.store.update(genre.id, result);
    });
}
}