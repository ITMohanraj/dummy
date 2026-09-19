import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="auth-page">
      <div class="auth-card-container">
        <!-- Brand Header -->
        <div class="auth-brand-header">
          <div class="brand-logo">
            <span class="material-symbols-outlined">eco</span>
          </div>
          <h2>Welcome Back to <span class="text-primary">CropDeal</span></h2>
          <p class="auth-subtitle">Buy and sell fresh crops directly with verified mandi rates</p>
        </div>

        <!-- Alert Notification -->
        <div *ngIf="errorMessage" class="alert alert-error">
          <span class="material-symbols-outlined text-sm">error</span>
          <span>{{ errorMessage }}</span>
        </div>

        <!-- Login Form -->
        <form [formGroup]="loginForm" (ngSubmit)="onSubmit()" class="auth-form">
          <div class="form-group">
            <label class="form-label" for="email">Email or Mobile Number</label>
            <div class="input-with-icon">
              <span class="material-symbols-outlined input-icon">mail</span>
              <input 
                id="email"
                type="text" 
                class="form-control" 
                formControlName="email" 
                placeholder="name@cropdeal.com"
                [class.is-invalid]="f['email'].touched && f['email'].invalid"
              />
            </div>
            <div *ngIf="f['email'].touched && f['email'].invalid" class="form-error">
              <span *ngIf="f['email'].errors?.['required']">Email or mobile number is required</span>
              <span *ngIf="f['email'].errors?.['email']">Please enter a valid email address</span>
            </div>
          </div>

          <div class="form-group">
            <div class="flex justify-between items-center">
              <label class="form-label" for="password">Password</label>
              <a href="javascript:void(0)" (click)="forgotPassword()" class="forgot-link">Forgot password?</a>
            </div>
            <div class="input-with-icon">
              <span class="material-symbols-outlined input-icon">lock</span>
              <input 
                id="password"
                [type]="showPassword ? 'text' : 'password'" 
                class="form-control" 
                formControlName="password" 
                placeholder="Enter your password"
                [class.is-invalid]="f['password'].touched && f['password'].invalid"
              />
              <button 
                type="button" 
                class="password-toggle" 
                (click)="showPassword = !showPassword"
                [attr.aria-label]="showPassword ? 'Hide password' : 'Show password'"
              >
                <span class="material-symbols-outlined">{{ showPassword ? 'visibility_off' : 'visibility' }}</span>
              </button>
            </div>
            <div *ngIf="f['password'].touched && f['password'].invalid" class="form-error">
              <span *ngIf="f['password'].errors?.['required']">Password is required</span>
              <span *ngIf="f['password'].errors?.['minlength']">Password must be at least 6 characters</span>
            </div>
          </div>

          <!-- Submit Button -->
          <button type="submit" class="btn btn-primary btn-lg w-full submit-btn" [disabled]="loading || loginForm.invalid">
            <span *ngIf="loading" class="spinner-sm"></span>
            <span>{{ loading ? 'Signing In...' : 'Login to CropDeal' }}</span>
            <span *ngIf="!loading" class="material-symbols-outlined text-sm">arrow_forward</span>
          </button>
        </form>

        <!-- Divider -->
        <div class="auth-divider">
          <span>New to CropDeal?</span>
        </div>

        <!-- Register Link -->
        <div class="register-prompt text-center">
          <p>Don't have an account? <a routerLink="/register" class="register-link font-bold">Register as Farmer or Dealer</a></p>
        </div>

        <!-- Trust Badges Footer -->
        <div class="trust-badges">
          <div class="trust-item">
            <span class="material-symbols-outlined text-emerald-600">verified_user</span>
            <span>Govt Mandi Sync</span>
          </div>
          <div class="trust-item">
            <span class="material-symbols-outlined text-emerald-600">lock_reset</span>
            <span>Escrow Protected</span>
          </div>
          <div class="trust-item">
            <span class="material-symbols-outlined text-emerald-600">local_shipping</span>
            <span>Direct Logistics</span>
          </div>
        </div>

      </div>
    </div>
  `,
  styles: [`
    .auth-page {
      min-height: calc(100vh - 140px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: 3rem 1.25rem;
      background: linear-gradient(135deg, #f0fdf4 0%, #f8fafc 100%);
    }
    .auth-card-container {
      width: 100%;
      max-width: 460px;
      background: #ffffff;
      border-radius: var(--radius-xl);
      border: 1px solid var(--border-light);
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08), 0 8px 10px -6px rgba(0, 0, 0, 0.04);
      padding: 2.5rem 2rem;
    }
    .auth-brand-header {
      text-align: center;
      margin-bottom: 2rem;
    }
    .brand-logo {
      width: 54px;
      height: 54px;
      margin: 0 auto 1rem auto;
      border-radius: var(--radius-lg);
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 10px rgba(21, 128, 61, 0.3);
    }
    .brand-logo span { font-size: 32px; }
    .auth-brand-header h2 {
      font-size: 1.5rem;
      font-weight: 800;
      color: var(--dark);
      margin-bottom: 0.35rem;
    }
    .auth-subtitle {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    .input-with-icon {
      position: relative;
      display: flex;
      align-items: center;
    }
    .input-icon {
      position: absolute;
      left: 12px;
      color: var(--text-light);
      font-size: 1.25rem;
      pointer-events: none;
    }
    .input-with-icon .form-control {
      padding-left: 2.5rem;
      padding-right: 2.5rem;
    }
    .password-toggle {
      position: absolute;
      right: 12px;
      background: none;
      border: none;
      color: var(--text-light);
      cursor: pointer;
      display: flex;
      align-items: center;
      padding: 2px;
    }
    .password-toggle:hover { color: var(--text-main); }
    .forgot-link {
      font-size: 0.8125rem;
      color: var(--primary);
      font-weight: 600;
    }
    .forgot-link:hover { text-decoration: underline; }
    .submit-btn {
      margin-top: 1.25rem;
      height: 48px;
    }
    .alert-error {
      background: #fef2f2;
      border: 1px solid #fee2e2;
      color: #991b1b;
      padding: 0.75rem 1rem;
      border-radius: var(--radius-md);
      font-size: 0.875rem;
      display: flex;
      align-items: center;
      gap: 0.5rem;
      margin-bottom: 1.25rem;
    }
    .auth-divider {
      text-align: center;
      margin: 1.75rem 0 1.25rem 0;
      position: relative;
    }
    .auth-divider::before {
      content: '';
      position: absolute;
      top: 50%;
      left: 0;
      right: 0;
      height: 1px;
      background: var(--border-light);
    }
    .auth-divider span {
      background: #ffffff;
      padding: 0 0.75rem;
      color: var(--text-muted);
      font-size: 0.8125rem;
      position: relative;
    }
    .register-prompt {
      font-size: 0.875rem;
      color: var(--text-muted);
    }
    .register-link {
      color: var(--primary);
    }
    .register-link:hover {
      text-decoration: underline;
    }
    .trust-badges {
      display: flex;
      justify-content: space-around;
      border-top: 1px solid var(--border-light);
      padding-top: 1.25rem;
      margin-top: 1.5rem;
    }
    .trust-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.6875rem;
      font-weight: 600;
      color: var(--text-muted);
      text-align: center;
    }
    .trust-item span.material-symbols-outlined { font-size: 1.15rem; }
    .spinner-sm {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #ffffff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loginForm: FormGroup = this.fb.group({
    email: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(4)]]
  });

  showPassword = false;
  loading = false;
  errorMessage = '';

  get f() { return this.loginForm.controls; }

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    const { email, password } = this.loginForm.value;

    this.authService.login({ email, password }).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.success(`Welcome back, ${res.fullName}!`);

        // Check if there is a returnUrl or redirect to role dashboard
        const returnUrl = this.route.snapshot.queryParams['returnUrl'];
        if (returnUrl) {
          this.router.navigateByUrl(returnUrl);
          return;
        }

        // Check profile completion
        if (!this.authService.isProfileCompleted()) {
          this.router.navigate(['/profile/complete']);
          return;
        }

        // Automatic redirect according to authenticated user role
        switch (res.role) {
          case 'FARMER':
            this.router.navigate(['/farmer/dashboard']);
            break;
          case 'DEALER':
            this.router.navigate(['/dealer/dashboard']);
            break;
          case 'DELIVERY_PARTNER':
            this.router.navigate(['/delivery/dashboard']);
            break;
          case 'ADMIN':
            this.router.navigate(['/admin/dashboard']);
            break;
          default:
            this.router.navigate(['/']);
        }
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Invalid credentials. Please check your email/mobile and password.';
        this.toast.error(this.errorMessage);
      }
    });
  }

  forgotPassword(): void {
    const email = this.loginForm.get('email')?.value;
    if (!email) {
      this.toast.info('Please enter your registered email address first.');
      return;
    }
    this.authService.forgotPassword({ email }).subscribe({
      next: () => this.toast.success('Password reset link sent to your email.'),
      error: () => this.toast.error('Unable to send reset email. Please try again.')
    });
  }
}
