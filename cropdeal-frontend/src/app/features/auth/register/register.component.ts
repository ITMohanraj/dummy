import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Role } from '../../../core/models/auth.models';

@Component({
  selector: 'app-register',
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
          <h2>Create your <span class="text-primary">CropDeal</span> Account</h2>
          <p class="auth-subtitle">Join thousands of verified farmers and dealers across India</p>
        </div>

        <!-- Alert Notification -->
        <div *ngIf="errorMessage" class="alert alert-error">
          <span class="material-symbols-outlined text-sm">error</span>
          <span>{{ errorMessage }}</span>
        </div>

        <form [formGroup]="registerForm" (ngSubmit)="onSubmit()" class="auth-form">
          <!-- Role Selection Segment -->
          <div class="form-group">
            <label class="form-label">I want to register as:</label>
            <div class="role-selector-grid">
              <label class="role-option-card" [class.selected]="selectedRole === 'FARMER'">
                <input type="radio" formControlName="role" value="FARMER" class="sr-only" (change)="onRoleChange('FARMER')">
                <span class="material-symbols-outlined role-icon">agriculture</span>
                <span class="role-name">Farmer / Producer</span>
                <span class="role-desc">Sell crops directly at fair mandi rates</span>
              </label>

              <label class="role-option-card" [class.selected]="selectedRole === 'DEALER'">
                <input type="radio" formControlName="role" value="DEALER" class="sr-only" (change)="onRoleChange('DEALER')">
                <span class="material-symbols-outlined role-icon">storefront</span>
                <span class="role-name">Dealer / Buyer</span>
                <span class="role-desc">Purchase bulk produce with escrow security</span>
              </label>

              <label class="role-option-card" [class.selected]="selectedRole === 'DELIVERY_PARTNER'">
                <input type="radio" formControlName="role" value="DELIVERY_PARTNER" class="sr-only" (change)="onRoleChange('DELIVERY_PARTNER')">
                <span class="material-symbols-outlined role-icon">local_shipping</span>
                <span class="role-name">Delivery Partner</span>
                <span class="role-desc">Transport farm harvest & earn fees</span>
              </label>
            </div>
          </div>

          <!-- Personal Information Grid -->
          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label" for="fullName">Full Name</label>
              <input 
                id="fullName"
                type="text" 
                class="form-control" 
                formControlName="fullName" 
                placeholder="Ramesh Kumar"
                [class.is-invalid]="f['fullName'].touched && f['fullName'].invalid"
              />
              <div *ngIf="f['fullName'].touched && f['fullName'].invalid" class="form-error">
                Full name is required
              </div>
            </div>

            <div class="form-group">
              <label class="form-label" for="phone">Mobile Phone</label>
              <input 
                id="phone"
                type="tel" 
                class="form-control" 
                formControlName="phoneNumber" 
                placeholder="9876543210"
                [class.is-invalid]="f['phoneNumber'].touched && f['phoneNumber'].invalid"
              />
              <div *ngIf="f['phoneNumber'].touched && f['phoneNumber'].invalid" class="form-error">
                Valid 10-digit mobile number required
              </div>
            </div>
          </div>

          <div class="form-group">
            <label class="form-label" for="email">Email Address</label>
            <input 
              id="email"
              type="email" 
              class="form-control" 
              formControlName="email" 
              placeholder="ramesh@cropdeal.com"
              [class.is-invalid]="f['email'].touched && f['email'].invalid"
            />
            <div *ngIf="f['email'].touched && f['email'].invalid" class="form-error">
              Valid email is required
            </div>
          </div>

          <!-- Passwords -->
          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label" for="password">Password</label>
              <input 
                id="password"
                type="password" 
                class="form-control" 
                formControlName="password" 
                placeholder="At least 6 characters"
                [class.is-invalid]="f['password'].touched && f['password'].invalid"
              />
              <div *ngIf="f['password'].touched && f['password'].invalid" class="form-error">
                Password must be at least 6 characters
              </div>
            </div>

            <div class="form-group">
              <label class="form-label" for="confirmPassword">Confirm Password</label>
              <input 
                id="confirmPassword"
                type="password" 
                class="form-control" 
                formControlName="confirmPassword" 
                placeholder="Repeat password"
                [class.is-invalid]="registerForm.hasError('mismatch') && f['confirmPassword'].touched"
              />
              <div *ngIf="registerForm.hasError('mismatch') && f['confirmPassword'].touched" class="form-error">
                Passwords do not match
              </div>
            </div>
          </div>

          <!-- Location Details -->
          <div class="form-row-2">
            <div class="form-group">
              <label class="form-label" for="state">State</label>
              <select id="state" class="form-control" formControlName="state">
                <option value="">Select State</option>
                <option value="Tamil Nadu">Tamil Nadu</option>
                <option value="Karnataka">Karnataka</option>
                <option value="Maharashtra">Maharashtra</option>
                <option value="Punjab">Punjab</option>
                <option value="Uttar Pradesh">Uttar Pradesh</option>
                <option value="Gujarat">Gujarat</option>
                <option value="Andhra Pradesh">Andhra Pradesh</option>
                <option value="Telangana">Telangana</option>
                <option value="Madhya Pradesh">Madhya Pradesh</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label" for="district">District / City</label>
              <input 
                id="district"
                type="text" 
                class="form-control" 
                formControlName="district" 
                placeholder="e.g. Erode, Salem, Pune"
              />
            </div>
          </div>

          <!-- Terms -->
          <div class="form-group terms-checkbox">
            <label class="flex items-center gap-2 text-xs text-muted">
              <input type="checkbox" formControlName="acceptTerms" class="checkbox">
              <span>I agree to CropDeal's Terms of Service and Escrow Guarantee Protection.</span>
            </label>
          </div>

          <!-- Submit Button -->
          <button type="submit" class="btn btn-primary btn-lg w-full submit-btn" [disabled]="loading || registerForm.invalid">
            <span *ngIf="loading" class="spinner-sm"></span>
            <span>{{ loading ? 'Creating Account...' : 'Create Account & Continue' }}</span>
          </button>
        </form>

        <div class="auth-divider">
          <span>Already have an account?</span>
        </div>

        <div class="text-center">
          <p class="text-sm text-muted">Already registered? <a routerLink="/login" class="font-bold text-primary">Login to your account</a></p>
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
      max-width: 600px;
      background: #ffffff;
      border-radius: var(--radius-xl);
      border: 1px solid var(--border-light);
      box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08);
      padding: 2.5rem 2rem;
    }
    .auth-brand-header {
      text-align: center;
      margin-bottom: 1.75rem;
    }
    .brand-logo {
      width: 50px;
      height: 50px;
      margin: 0 auto 0.75rem auto;
      border-radius: var(--radius-lg);
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 4px 10px rgba(21, 128, 61, 0.3);
    }
    .brand-logo span { font-size: 28px; }
    .auth-brand-header h2 { font-size: 1.4rem; font-weight: 800; color: var(--dark); }
    .auth-subtitle { font-size: 0.875rem; color: var(--text-muted); }
    .role-selector-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.75rem;
      margin-top: 0.5rem;
    }
    .role-option-card {
      border: 2px solid var(--border-light);
      border-radius: var(--radius-md);
      padding: 0.875rem 0.5rem;
      text-align: center;
      cursor: pointer;
      display: flex;
      flex-direction: column;
      align-items: center;
      transition: all 0.2s ease;
      background: #ffffff;
    }
    .role-option-card:hover { border-color: var(--primary-light); background: var(--primary-subtle); }
    .role-option-card.selected {
      border-color: var(--primary);
      background: var(--primary-subtle);
      box-shadow: 0 0 0 1px var(--primary);
    }
    .role-icon { font-size: 1.75rem; color: var(--primary); margin-bottom: 0.35rem; }
    .role-name { font-size: 0.8125rem; font-weight: 700; color: var(--dark); display: block; }
    .role-desc { font-size: 0.6875rem; color: var(--text-muted); line-height: 1.2; margin-top: 0.25rem; }
    .form-row-2 {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
    }
    .sr-only { position: absolute; width: 1px; height: 1px; padding: 0; margin: -1px; overflow: hidden; clip: rect(0,0,0,0); border: 0; }
    .submit-btn { margin-top: 1rem; height: 48px; }
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
      margin: 1.5rem 0 1rem 0;
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
    @media (max-width: 640px) {
      .role-selector-grid { grid-template-columns: 1fr; }
      .form-row-2 { grid-template-columns: 1fr; }
    }
  `]
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  selectedRole: Role = 'FARMER';
  loading = false;
  errorMessage = '';

  registerForm: FormGroup = this.fb.group({
    role: ['FARMER', [Validators.required]],
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', [Validators.required]],
    state: ['Tamil Nadu', [Validators.required]],
    district: ['Erode', [Validators.required]],
    acceptTerms: [true, [Validators.requiredTrue]]
  }, { validators: this.passwordMatchValidator });

  get f() { return this.registerForm.controls; }

  onRoleChange(role: Role): void {
    this.selectedRole = role;
    this.registerForm.patchValue({ role });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.loading = true;
    this.errorMessage = '';

    const val = this.registerForm.value;

    this.authService.register({
      fullName: val.fullName,
      email: val.email,
      password: val.password,
      phoneNumber: val.phoneNumber,
      role: val.role,
      state: val.state,
      district: val.district
    }).subscribe({
      next: (res) => {
        this.loading = false;
        this.toast.success(`Account created successfully! Welcome to CropDeal, ${res.fullName}`);
        
        // Redirect to profile completion wizard
        this.router.navigate(['/profile/complete']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = err?.error?.message || 'Registration failed. Email or phone may already exist.';
        this.toast.error(this.errorMessage);
      }
    });
  }
}
