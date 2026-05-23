import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

/**
 * Functional Route Guard checking for active login session and role eligibility.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    // Check for role restrictions on the route configuration
    const expectedRoles = route.data['roles'] as string[];
    if (expectedRoles && !authService.hasRole(expectedRoles)) {
      // Access denied: redirect to dashboard
      router.navigate(['/dashboard']);
      return false;
    }
    return true;
  }

  // Not logged in: redirect to login
  router.navigate(['/login'], { queryParams: { returnUrl: state.url } });
  return false;
};
