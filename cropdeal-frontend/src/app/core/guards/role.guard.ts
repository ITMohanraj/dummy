import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { Role } from '../models/auth.models';
import { ToastService } from '../services/toast.service';

export const roleGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const toast = inject(ToastService);

  const expectedRoles: Role[] = route.data['roles'] || [];

  if (!authService.isAuthenticated()) {
    router.navigate(['/login']);
    return false;
  }

  if (expectedRoles.length > 0 && !authService.hasRole(expectedRoles)) {
    toast.warning(`Access restricted to [${expectedRoles.join(', ')}] users only.`);
    router.navigate(['/']);
    return false;
  }

  return true;
};
