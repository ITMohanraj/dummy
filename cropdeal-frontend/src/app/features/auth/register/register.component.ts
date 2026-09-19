import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { Role } from '../../../core/models/auth.models';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule],
  template: `
    <div class="auth-page py-12">
      <div class="container max-w-lg">
        
        <div class="auth-card card p-8">
          
          <div class="text-center mb-6">
            <h1 class="auth-title">Create your CropDeal Account</h1>
            <p class="text-xs text-muted mt-1">Join India's direct agricultural marketplace</p>
          </div>

          <!-- Role Selection Pills -->
          <div class="role-pills-grid mb-6">
            <button 
              type="button" 
              class="role-pill-btn" 
              [class.active]="registerForm.get('role')?.value === 'FARMER'"
              (click)="setRole('FARMER')"
            >
              <span class="material-symbols-outlined">agriculture</span>
              <span>Farmer</span>
            </button>
            <button 
              type="button" 
              class="role-pill-btn" 
              [class.active]="registerForm.get('role')?.value === 'DEALER'"
              (click)="setRole('DEALER')"
            >
              <span class="material-symbols-outlined">storefront</span>
              <span>Dealer</span>
            </button>
            <button 
              type="button" 
              class="role-pill-btn" 
              [class.active]="registerForm.get('role')?.value === 'DELIVERY_PARTNER'"
              (click)="setRole('DELIVERY_PARTNER')"
            >
              <span class="material-symbols-outlined">local_shipping</span>
              <span>Delivery</span>
            </button>
          </div>

          <form [formGroup]="registerForm" (ngSubmit)="onSubmit()">
            
            <div class="form-group">
              <label class="form-label">Full Name</label>
              <input 
                type="text" 
                formControlName="fullName"
                placeholder="Ramesh Kumar"
                class="form-control"
              />
            </div>

            <div class="form-group">
              <label class="form-label">Email Address</label>
              <input 
                type="email" 
                formControlName="email"
                placeholder="ramesh@gmail.com"
                class="form-control"
              />
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div class="form-group">
                <label class="form-label">Phone Number</label>
                <input 
                  type="text" 
                  formControlName="phoneNumber"
                  placeholder="9876543210"
                  class="form-control"
                />
              </div>
              <div class="form-group">
                <label class="form-label">State</label>
                <input 
                  type="text" 
                  formControlName="state"
                  placeholder="Tamil Nadu"
                  class="form-control"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">District / Location</label>
              <input 
                type="text" 
                formControlName="district"
                placeholder="Salem"
                class="form-control"
              />
            </div>

            <div class="form-group">
              <label class="form-label">Password</label>
              <input 
                type="password" 
                formControlName="password"
                placeholder="••••••••"
                class="form-control"
              />
            </div>

            <button 
              type="submit" 
              class="btn btn-primary btn-lg w-full mt-4"
              [disabled]="registerForm.invalid || isSubmitting"
            >
              {{ isSubmitting ? 'Registering Account...' : 'Complete Registration' }}
            </button>

          </form>

          <div class="auth-footer text-center mt-6 pt-6 border-t border-slate-100 text-xs text-muted">
            Already have an account? 
            <a routerLink="/login" class="text-emerald-700 font-bold ml-1">Sign In Here</a>
          </div>

        </div>

      </div>
    </div>
  `,
  styles: [`
    .auth-title { font-size: 1.5rem; font-weight: 800; color: var(--dark); }
    .role-pills-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: 0.5rem;
    }
    .role-pill-btn {
      background: #f8fafc;
      border: 1px solid var(--border-strong);
      border-radius: var(--radius-md);
      padding: 0.75rem 0.5rem;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 0.25rem;
      font-size: 0.8125rem;
      font-weight: 700;
      color: var(--text-muted);
      cursor: pointer;
    }
    .role-pill-btn span:first-child { font-size: 24px; }
    .role-pill-btn.active {
      background: var(--primary-subtle);
      border-color: var(--primary);
      color: var(--primary-dark);
    }
  `]
})
export class RegisterComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private toast = inject(ToastService);

  isSubmitting = false;

  registerForm = this.fb.group({
    fullName: ['', [Validators.required]],
    email: ['', [Validators.required, Validators.email]],
    password: ['Password123!', [Validators.required, Validators.minLength(6)]],
    phoneNumber: ['9876543210', [Validators.required]],
    role: ['FARMER' as Role, [Validators.required]],
    state: ['Tamil Nadu', [Validators.required]],
    district: ['Salem', [Validators.required]]
  });

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['role']) {
        this.setRole(params['role'] as Role);
      }
    });
  }

  setRole(role: Role): void {
    this.registerForm.patchValue({ role });
  }

  onSubmit(): void {
    if (this.registerForm.invalid) return;

    this.isSubmitting = true;
    this.authService.register({
      fullName: this.registerForm.value.fullName!,
      email: this.registerForm.value.email!,
      password: this.registerForm.value.password!,
      phoneNumber: this.registerForm.value.phoneNumber!,
      role: this.registerForm.value.role as Role,
      state: this.registerForm.value.state!,
      district: this.registerForm.value.district!
    }).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.toast.success('Registration successful! Welcome to CropDeal.');
        if (res.role === 'FARMER') this.router.navigate(['/farmer/dashboard']);
        else if (res.role === 'DEALER') this.router.navigate(['/dealer/dashboard']);
        else if (res.role === 'DELIVERY_PARTNER') this.router.navigate(['/delivery/dashboard']);
        else this.router.navigate(['/']);
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }
}
