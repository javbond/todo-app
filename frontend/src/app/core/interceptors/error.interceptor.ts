import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ErrorHandlerService } from '../services/error-handler.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  // Placeholder — passes through for Sprint 1
  const errorHandler = inject(ErrorHandlerService);

  return next(req).pipe(
    catchError(error => {
      errorHandler.handle(error);
      return throwError(() => error);
    })
  );
};
