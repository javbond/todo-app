import { Injectable } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ErrorResponse } from '../../shared/models/api-response.model';

@Injectable({ providedIn: 'root' })
export class ErrorHandlerService {
  handle(error: HttpErrorResponse): void {
    // Placeholder — will be expanded in Sprint 2
    const apiError = error.error as ErrorResponse;
    if (apiError?.message) {
      console.error(`[${apiError.code}] ${apiError.message}`);
    } else {
      console.error('An unexpected error occurred', error.message);
    }
  }
}
