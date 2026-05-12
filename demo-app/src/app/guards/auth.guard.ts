import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { LoginStore } from '../features/login/login.store';

export const guestGuard: CanActivateFn = () => {
  const router = inject(Router);
  // Check if a token exists in local storage
  const token = localStorage.getItem('token'); 

  if (!token) {
    return true; // No token found, user is a guest, allow access
  }

  // If token exists, redirect to home/books instead of staying on login/forgot-password
  return router.parseUrl('/books');
};




export const authGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  return loginStore.isAuthenticated() ? true : router.createUrlTree(['/login']);
};

// export const guestGuard: CanActivateFn = () => {
//   const loginStore = inject(LoginStore);
//   const router = inject(Router);

//   if (!loginStore.isAuthenticated()) return true;

//   // Redirect already-authenticated users to their role-appropriate landing page
//   const role = loginStore.role();
//   return router.createUrlTree(role === 'ADMIN' ? ['/people'] : ['/books']);
// };

/** Blocks non-ADMIN users from accessing admin-only routes */
export const adminGuard: CanActivateFn = () => {
  const loginStore = inject(LoginStore);
  const router = inject(Router);

  return loginStore.role() === 'ADMIN' ? true : router.createUrlTree(['/books']);
};


