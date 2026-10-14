import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from './auth.service';
import { catchError, throwError } from 'rxjs';

// Attach the current JWT to API calls except login/registration.
// A 401 means the server rejected the session, so clear it in the browser.
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService);
  const token = auth.activeSession()?.token;
  const protectedRequest = !!token && request.url.startsWith('/api/') && !request.url.startsWith('/api/v1/auth/');
  return next(protectedRequest ? request.clone({setHeaders: {Authorization: `Bearer ${token}`}}) : request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (protectedRequest && error.status === 401) auth.logout();
      return throwError(() => error);
    })
  );
};
