import { inject } from '@angular/core';
import { CanActivateFn } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = () => {
  // Placeholder — returns true for Sprint 1 (no auth required yet)
  const authService = inject(AuthService);
  void authService;
  return true;
};
