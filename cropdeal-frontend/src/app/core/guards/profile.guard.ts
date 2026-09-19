import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const profileGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
    return false;
  }

  // If user profile is incomplete, redirect to profile complete page
  if (!authService.isProfileCompleted() && state.url !== '/profile/complete') {
    router.navigate(['/profile/complete']);
    return false;
  }

  return true;
};
