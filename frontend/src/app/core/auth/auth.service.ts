import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { TokenStorageService } from './token-storage.service';
import {
  AuthResponse,
  LoginRequest,
  LogoutRequest,
  MessageResponse,
  RefreshRequest,
  RegisterRequest,
  ForgotPasswordRequest,
  ResetPasswordRequest
} from '../../shared/models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly tokenStorage = inject(TokenStorageService);
  private readonly baseUrl = `${environment.apiUrl}/auth`;

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/register`, request).pipe(
      tap(response => this.tokenStorage.setTokens(response.accessToken, response.refreshToken))
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/login`, request).pipe(
      tap(response => this.tokenStorage.setTokens(response.accessToken, response.refreshToken))
    );
  }

  logout(): Observable<void> {
    const refreshToken = this.tokenStorage.getRefreshToken() ?? '';
    const request: LogoutRequest = { refreshToken };
    return this.http.post<void>(`${this.baseUrl}/logout`, request).pipe(
      tap(() => this.tokenStorage.clearTokens())
    );
  }

  refresh(request: RefreshRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/refresh`, request).pipe(
      tap(response => this.tokenStorage.setTokens(response.accessToken, response.refreshToken))
    );
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${this.baseUrl}/reset-password`, request);
  }

  isAuthenticated(): boolean {
    return this.tokenStorage.isAuthenticated();
  }
}
