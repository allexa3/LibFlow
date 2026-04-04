import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'people',
  },
  {
    path: 'people',
    loadComponent: () =>
      import('./features/person-list/person-list-page.component').then(
        (m) => m.PersonListPageComponent,
      ),
  },
  {
    path: 'books',
    loadComponent: () =>
      import('./features/book-list/book-list-page.component').then(
        (m) => m.BookListPageComponent,
      ),
  },
  {
    path: 'genres',
    loadComponent: () =>
      import('./features/genre-list/genre-list-page.component').then(
        (m) => m.GenreListPageComponent,
      ),
  },
  {
    path: 'error',
    loadComponent: () =>
      import('./features/not-found/not-found-page.component').then(
        (m) => m.NotFoundPageComponent,
      ),
  },
  {
    path: '**',
    redirectTo: 'error',
  },
];
