import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'people',
    pathMatch: 'full'
  },
  {
    path: 'login',
    loadComponent: () => import('./features/login/login.component').then(m => m.LoginComponent),
    canActivate: [guestGuard] // Logged-in users can't go back to login
  },
  {
    path: 'people',
    loadComponent: () => import('./features/person-list/person-list-page.component').then(m => m.PersonListPageComponent),
    canActivate: [authGuard] // Protected: Requires login
  },
  {
    path: 'books',
    loadComponent: () => import('./features/book-list/book-list-page.component').then(m => m.BookListPageComponent),
    canActivate: [authGuard] // Protected: Requires login
  },
  {
    path: 'genres',
    loadComponent: () => import('./features/genre-list/genre-list-page.component').then(m => m.GenreListPageComponent),
    canActivate: [authGuard] // Protected: Requires login
  },
  {
    path: '**',
    loadComponent: () => import('./features/not-found/not-found-page.component').then(m => m.NotFoundPageComponent)
  }
];