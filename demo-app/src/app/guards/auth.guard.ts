import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginStore } from '../features/login/login.store';

export const guestGuard: CanActivateFn = () => {
  const router = inject(Router);
  const token = localStorage.getItem('token'); 

  if (!token) {
    return true;
  }

  return router.parseUrl('/books');
};




export const authGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  return loginStore.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

export const adminGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  return loginStore.role() === 'ADMIN' ? true : router.createUrlTree(['/books']);
};


