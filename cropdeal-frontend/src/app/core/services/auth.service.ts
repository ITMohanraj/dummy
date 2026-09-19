import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, Role, UserProfile, ForgotPasswordRequest, ResetPasswordRequest, MessageResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly TOKEN_KEY = 'cropdeal_token';
  private readonly USER_KEY = 'cropdeal_user';

  currentUser = signal<AuthResponse | null>(this.getStoredUser());
  isAuthenticated = signal<boolean>(!!this.getToken());

  private getStoredUser(): AuthResponse | null {
    const raw = localStorage.getItem(this.USER_KEY);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  getUserId(): number | null {
    return this.currentUser()?.userId ?? null;
  }

  getUserRole(): Role | null {
    return this.currentUser()?.role ?? null;
  }

  isProfileCompleted(): boolean {
    const user = this.currentUser();
    if (!user) return false;
    return (user as any).isProfileCompleted ?? true;
  }

  setProfileCompleted(completed: boolean): void {
    const user = this.currentUser();
    if (user) {
      const updated = { ...user, isProfileCompleted: completed };
      localStorage.setItem(this.USER_KEY, JSON.stringify(updated));
      this.currentUser.set(updated);
    }
  }

  getRoleDashboardUrl(): string {
    const role = this.getUserRole();
    switch (role) {
      case 'FARMER':
        return '/farmer/dashboard';
      case 'DEALER':
        return '/dealer/dashboard';
      case 'DELIVERY_PARTNER':
        return '/delivery/dashboard';
      case 'ADMIN':
        return '/admin/dashboard';
      default:
        return '/';
    }
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiGatewayUrl}${environment.endpoints.auth}/register`, request)
      .pipe(
        tap(res => this.handleAuthSuccess(res))
      );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiGatewayUrl}${environment.endpoints.auth}/login`, request)
      .pipe(
        tap(res => this.handleAuthSuccess(res))
      );
  }

  forgotPassword(request: ForgotPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiGatewayUrl}${environment.endpoints.auth}/forgot-password`, request);
  }

  resetPassword(request: ResetPasswordRequest): Observable<MessageResponse> {
    return this.http.post<MessageResponse>(`${environment.apiGatewayUrl}${environment.endpoints.auth}/reset-password`, request);
  }

  getUserProfile(): Observable<UserProfile> {
    return this.http.get<UserProfile>(`${environment.apiGatewayUrl}${environment.endpoints.users}/profile`);
  }

  private handleAuthSuccess(res: AuthResponse): void {
    if (res && res.token) {
      localStorage.setItem(this.TOKEN_KEY, res.token);
      localStorage.setItem(this.USER_KEY, JSON.stringify(res));
      this.currentUser.set(res);
      this.isAuthenticated.set(true);
    }
  }

  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUser.set(null);
    this.isAuthenticated.set(false);
    this.router.navigate(['/login']);
  }

  hasRole(allowedRoles: Role[]): boolean {
    const role = this.getUserRole();
    return role ? allowedRoles.includes(role) : false;
  }
}
