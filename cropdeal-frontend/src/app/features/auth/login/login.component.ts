import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  template: `
    <div class="auth-page py-12">
      <div class="container max-w-md">
        
        <div class="auth-card card p-8">
          
          <div class="text-center mb-6">
            <div class="auth-icon-wrap mb-3">
              <span class="material-symbols-outlined">lock</span>
            </div>
            <h1 class="auth-title">Welcome Back to CropDeal</h1>
            <p class="text-xs text-muted mt-1">Sign in to manage your crops, orders, and wallet funds</p>
          </div>

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            
            <div class="form-group">
              <label class="form-label">Email Address</label>
              <input 
                type="email" 
                formControlName="email"
                placeholder="farmer@cropdeal.com"
                class="form-control"
              />
              <div *ngIf="loginForm.get('email')?.touched && loginForm.get('email')?.invalid" class="form-error">
                Please enter a valid email address.
              </div>
            </div>

            <div class="form-group">
              <div class="flex justify-between items-center mb-1">
                <label class="form-label mb-0">Password</label>
                <a routerLink="/forgot-password" class="text-xs text-emerald-700 font-semibold">Forgot Password?</a>
              </div>
              <input 
                type="password" 
                formControlName="password"
                placeholder="••••••••"
                class="form-control"
              />
              <div *ngIf="loginForm.get('password')?.touched && loginForm.get('password')?.invalid" class="form-error">
                Password is required.
              </div>
            </div>

            <button 
              type="submit" 
              class="btn btn-primary btn-lg w-full mt-4"
              [disabled]="loginForm.invalid || isSubmitting"
            >
              {{ isSubmitting ? 'Authenticating...' : 'Sign In' }}
            </button>

          </form>

          <div class="auth-footer text-center mt-6 pt-6 border-t border-slate-100 text-xs text-muted">
            Don't have an account? 
            <a routerLink="/register" class="text-emerald-700 font-bold ml-1">Create an Account</a>
          </div>

          <!-- Quick Test Accounts -->
          <div class="quick-demo-box mt-6 p-3 bg-slate-50 rounded-lg border border-slate-200">
            <div class="text-2xs font-bold text-muted uppercase mb-2">⚡ Quick 1-Click Role Login</div>
            <div class="grid grid-cols-2 gap-2">
              <button class="btn btn-secondary btn-sm text-xs" (click)="quickLogin('FARMER', 'Ramesh Kumar (Farmer)', 101)">
                🚜 Farmer
              </button>
              <button class="btn btn-secondary btn-sm text-xs" (click)="quickLogin('DEALER', 'Kisan Mandi Traders', 201)">
                🏪 Dealer
              </button>
              <button class="btn btn-secondary btn-sm text-xs" (click)="quickLogin('DELIVERY_PARTNER', 'AgriLogistics Express', 301)">
                🚚 Delivery
              </button>
              <button class="btn btn-secondary btn-sm text-xs" (click)="quickLogin('ADMIN', 'Platform Administrator', 999)">
                🛡️ Admin
              </button>
            </div>
          </div>

        </div>

      </div>
    </div>
  `,
  styles: [`
    .auth-title { font-size: 1.5rem; font-weight: 800; color: var(--dark); }
    .auth-icon-wrap {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary);
      display: inline-flex;
      align-items: center;
      justify-content: center;
    }
    .text-2xs { font-size: 0.625rem; }
  `]
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);

  isSubmitting = false;

  loginForm = this.fb.group({
    email: ['farmer@cropdeal.com', [Validators.required, Validators.email]],
    password: ['Password123!', [Validators.required]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) return;

    this.isSubmitting = true;
    this.authService.login({
      email: this.loginForm.value.email!,
      password: this.loginForm.value.password!
    }).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.toast.success(`Welcome back, ${res.fullName}!`);
        this.redirectByRole(res.role);
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }

  quickLogin(role: any, name: string, id: number): void {
    this.authService.switchRoleSession(role, name, id);
    this.toast.success(`Signed in as ${name}`);
    this.redirectByRole(role);
  }

  private redirectByRole(role: string): void {
    const returnUrl = this.route.snapshot.queryParams['returnUrl'];
    if (returnUrl) {
      this.router.navigateByUrl(returnUrl);
      return;
    }

    if (role === 'FARMER') this.router.navigate(['/farmer/dashboard']);
    else if (role === 'DEALER') this.router.navigate(['/dealer/dashboard']);
    else if (role === 'DELIVERY_PARTNER') this.router.navigate(['/delivery/dashboard']);
    else if (role === 'ADMIN') this.router.navigate(['/admin/dashboard']);
    else this.router.navigate(['/']);
  }
}
