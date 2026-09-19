import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { ToastService } from '../services/toast.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const toast = inject(ToastService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unexpected error occurred. Please try again.';

      if (error.error instanceof ErrorEvent) {
        // Client-side network error
        errorMessage = error.error.message;
      } else {
        // Backend HTTP error code
        switch (error.status) {
          case 400:
            errorMessage = error.error?.message || error.error?.error || 'Invalid request parameters.';
            break;
          case 401:
            errorMessage = 'Session expired or unauthorized. Please log in again.';
            authService.logout();
            break;
          case 403:
            errorMessage = 'You do not have permission to perform this action.';
            break;
          case 404:
            errorMessage = error.error?.message || 'Requested resource was not found.';
            break;
          case 409:
            errorMessage = error.error?.message || 'Conflict: Record already exists or is locked.';
            break;
          case 429:
            errorMessage = 'Rate limit exceeded. Please wait a moment before trying again.';
            break;
          case 500:
          case 502:
          case 503:
            errorMessage = 'Backend service currently unavailable. Please verify microservices are online.';
            break;
          default:
            errorMessage = error.error?.message || `Error ${error.status}: ${error.statusText}`;
        }
      }

      toast.error(errorMessage);
      return throwError(() => error);
    })
  );
};
