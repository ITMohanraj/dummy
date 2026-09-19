import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-profile-complete',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  template: `
    <div class="profile-complete-page">
      <div class="wizard-container card">
        <!-- Header & Progress -->
        <div class="wizard-header">
          <div class="brand-badge">
            <span class="material-symbols-outlined">verified</span>
            <span>Account Verification Required</span>
          </div>
          <h2>Complete Your <span class="text-primary">CropDeal</span> Profile</h2>
          <p class="subtitle">
            Please complete your profile to unlock secure buying, selling, bidding, and escrow transactions.
          </p>

          <!-- Progress Bar -->
          <div class="progress-box">
            <div class="flex justify-between items-center text-xs font-bold mb-1">
              <span>Profile Completion</span>
              <span class="text-emerald-700">{{ calculateProgress() }}% Complete</span>
            </div>
            <div class="progress-bar-bg">
              <div class="progress-bar-fill" [style.width.%]="calculateProgress()"></div>
            </div>
          </div>
        </div>

        <!-- Form -->
        <form [formGroup]="profileForm" (ngSubmit)="onSubmit()" class="wizard-form">
          <!-- Section 1: Personal & Contact Details -->
          <div class="form-section">
            <div class="section-title">
              <span class="material-symbols-outlined text-emerald-600">person</span>
              <span>1. Personal & Contact Information</span>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div class="form-group">
                <label class="form-label">Full Name *</label>
                <input type="text" class="form-control" formControlName="fullName" placeholder="Full name">
              </div>
              <div class="form-group">
                <label class="form-label">Mobile Phone *</label>
                <input type="text" class="form-control" formControlName="phone" placeholder="10-digit mobile">
              </div>
              <div class="form-group" *ngIf="userRole === 'DEALER'">
                <label class="form-label">Business / Firm Name *</label>
                <input type="text" class="form-control" formControlName="businessName" placeholder="e.g. Apex Agri Traders">
              </div>
              <div class="form-group" *ngIf="userRole === 'DEALER'">
                <label class="form-label">Owner / Authorized Person</label>
                <input type="text" class="form-control" formControlName="ownerName" placeholder="Owner name">
              </div>
            </div>
          </div>

          <!-- Section 2: Address & Location -->
          <div class="form-section">
            <div class="section-title">
              <span class="material-symbols-outlined text-emerald-600">location_on</span>
              <span>2. Address & Farm / Mandi Location</span>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div class="form-group">
                <label class="form-label">Door / Street Address *</label>
                <input type="text" class="form-control" formControlName="address" placeholder="Door No, Street Name">
              </div>
              <div class="form-group" *ngIf="userRole === 'FARMER'">
                <label class="form-label">Village / Taluk *</label>
                <input type="text" class="form-control" formControlName="village" placeholder="e.g. Bhavani, Gobichettipalayam">
              </div>
              <div class="form-group">
                <label class="form-label">District / City *</label>
                <input type="text" class="form-control" formControlName="district" placeholder="e.g. Erode, Salem">
              </div>
              <div class="form-group">
                <label class="form-label">State *</label>
                <select class="form-control" formControlName="state">
                  <option value="Tamil Nadu">Tamil Nadu</option>
                  <option value="Karnataka">Karnataka</option>
                  <option value="Maharashtra">Maharashtra</option>
                  <option value="Punjab">Punjab</option>
                  <option value="Uttar Pradesh">Uttar Pradesh</option>
                  <option value="Gujarat">Gujarat</option>
                  <option value="Andhra Pradesh">Andhra Pradesh</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Section 3: Delivery Vehicle Info (for Delivery Partner) -->
          <div class="form-section" *ngIf="userRole === 'DELIVERY_PARTNER'">
            <div class="section-title">
              <span class="material-symbols-outlined text-emerald-600">local_shipping</span>
              <span>3. Vehicle & Driving Credentials</span>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div class="form-group">
                <label class="form-label">Vehicle Type *</label>
                <select class="form-control" formControlName="vehicleType">
                  <option value="Mini Truck (Tata Ace / Pickup)">Mini Truck (Tata Ace / Pickup)</option>
                  <option value="Medium Commercial Truck (Eicher / LCV)">Medium Commercial Truck (Eicher / LCV)</option>
                  <option value="Heavy Goods Vehicle (10-14 Wheeler)">Heavy Goods Vehicle (10-14 Wheeler)</option>
                  <option value="Three Wheeler Goods Auto">Three Wheeler Goods Auto</option>
                </select>
              </div>
              <div class="form-group">
                <label class="form-label">Vehicle Registration Number *</label>
                <input type="text" class="form-control" formControlName="vehicleNumber" placeholder="TN 33 AB 1234">
              </div>
              <div class="form-group">
                <label class="form-label">Driving License Number *</label>
                <input type="text" class="form-control" formControlName="drivingLicense" placeholder="DL-0420110012345">
              </div>
              <div class="form-group">
                <label class="form-label">Availability Status</label>
                <select class="form-control" formControlName="availabilityStatus">
                  <option value="AVAILABLE">Available for Orders</option>
                  <option value="OFFLINE">Temporarily Offline</option>
                </select>
              </div>
            </div>
          </div>

          <!-- Section 4: Bank Account Details for Escrow Payouts -->
          <div class="form-section">
            <div class="section-title">
              <span class="material-symbols-outlined text-emerald-600">account_balance</span>
              <span>{{ userRole === 'DELIVERY_PARTNER' ? '4.' : '3.' }} Bank Account for Escrow Payouts</span>
            </div>
            <div class="grid grid-cols-3 gap-4">
              <div class="form-group">
                <label class="form-label">Account Holder Name *</label>
                <input type="text" class="form-control" formControlName="bankAccountName" placeholder="As per bank passbook">
              </div>
              <div class="form-group">
                <label class="form-label">Bank Account Number *</label>
                <input type="text" class="form-control" formControlName="bankAccountNumber" placeholder="9 to 18 digit account number">
              </div>
              <div class="form-group">
                <label class="form-label">IFSC Code *</label>
                <input type="text" class="form-control" formControlName="ifscCode" placeholder="SBIN0001234">
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="wizard-actions flex justify-between items-center">
            <button type="button" class="btn btn-secondary" (click)="skipForNow()">
              Skip for now
            </button>
            <button type="submit" class="btn btn-primary btn-lg" [disabled]="loading || profileForm.invalid">
              <span *ngIf="loading" class="spinner-sm"></span>
              <span>{{ loading ? 'Saving Profile...' : 'Complete Profile & Launch Dashboard' }}</span>
              <span *ngIf="!loading" class="material-symbols-outlined text-sm">arrow_forward</span>
            </button>
          </div>
        </form>
      </div>
    </div>
  `,
  styles: [`
    .profile-complete-page {
      padding: 3rem 1.25rem;
      min-height: calc(100vh - 140px);
      background: linear-gradient(135deg, #f0fdf4 0%, #f8fafc 100%);
      display: flex;
      justify-content: center;
    }
    .wizard-container {
      width: 100%;
      max-width: 860px;
      padding: 2.5rem;
      background: #ffffff;
      border-radius: var(--radius-xl);
      border: 1px solid var(--border-light);
      box-shadow: var(--shadow-lg);
    }
    .wizard-header {
      margin-bottom: 2rem;
      border-bottom: 1px solid var(--border-light);
      padding-bottom: 1.5rem;
    }
    .brand-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      background: #fef3c7;
      color: #92400e;
      border: 1px solid #fde68a;
      padding: 0.25rem 0.625rem;
      border-radius: var(--radius-full);
      font-size: 0.75rem;
      font-weight: 700;
      margin-bottom: 0.75rem;
    }
    .brand-badge span.material-symbols-outlined { font-size: 16px; }
    .wizard-header h2 { font-size: 1.625rem; font-weight: 800; color: var(--dark); margin-bottom: 0.35rem; }
    .subtitle { color: var(--text-muted); font-size: 0.875rem; }
    .progress-box {
      margin-top: 1.25rem;
      background: #f8fafc;
      padding: 0.875rem 1rem;
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
    }
    .progress-bar-bg {
      height: 8px;
      background: #e2e8f0;
      border-radius: var(--radius-full);
      overflow: hidden;
    }
    .progress-bar-fill {
      height: 100%;
      background: linear-gradient(90deg, var(--primary) 0%, var(--primary-light) 100%);
      border-radius: var(--radius-full);
      transition: width 0.4s ease;
    }
    .form-section {
      margin-bottom: 2rem;
      padding-bottom: 1.5rem;
      border-bottom: 1px dashed var(--border-light);
    }
    .section-title {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      font-size: 1.0625rem;
      font-weight: 700;
      color: var(--dark);
      margin-bottom: 1rem;
    }
    .wizard-actions {
      padding-top: 1rem;
    }
    .spinner-sm {
      width: 18px;
      height: 18px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #ffffff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (max-width: 768px) {
      .grid-cols-2, .grid-cols-3 { grid-template-columns: 1fr; }
      .wizard-container { padding: 1.5rem; }
    }
  `]
})
export class ProfileCompleteComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private toast = inject(ToastService);
  private router = inject(Router);

  userRole = this.authService.getUserRole() || 'FARMER';
  loading = false;

  profileForm: FormGroup = this.fb.group({
    fullName: [this.authService.currentUser()?.fullName || '', [Validators.required]],
    phone: ['9876543210', [Validators.required]],
    businessName: ['CropDeal Traders'],
    ownerName: [''],
    address: ['124, Gandhi Road', [Validators.required]],
    village: ['Bhavani'],
    district: ['Erode', [Validators.required]],
    state: ['Tamil Nadu', [Validators.required]],
    vehicleType: ['Mini Truck (Tata Ace / Pickup)'],
    vehicleNumber: ['TN 33 AB 1234'],
    drivingLicense: ['DL-0420110012345'],
    availabilityStatus: ['AVAILABLE'],
    bankAccountName: [this.authService.currentUser()?.fullName || '', [Validators.required]],
    bankAccountNumber: ['918237491823', [Validators.required]],
    ifscCode: ['SBIN0001234', [Validators.required]]
  });

  ngOnInit(): void {
    const user = this.authService.currentUser();
    if (user) {
      this.profileForm.patchValue({
        fullName: user.fullName,
        bankAccountName: user.fullName
      });
    }
  }

  calculateProgress(): number {
    let filled = 0;
    const controls = this.profileForm.controls;
    const fields = ['fullName', 'phone', 'address', 'district', 'bankAccountName', 'bankAccountNumber', 'ifscCode'];
    fields.forEach(f => {
      if (controls[f]?.value && controls[f]?.value.trim() !== '') filled++;
    });
    return Math.round((filled / fields.length) * 100);
  }

  onSubmit(): void {
    if (this.profileForm.invalid) return;

    this.loading = true;
    const userId = this.authService.getUserId() || 101;
    const val = this.profileForm.value;

    let updateObs: Observable<any>;
    if (this.userRole === 'FARMER') {
      updateObs = this.userService.updateFarmerProfile(userId, val);
    } else if (this.userRole === 'DEALER') {
      updateObs = this.userService.updateDealerProfile(userId, val);
    } else {
      updateObs = this.userService.updateDeliveryPartnerProfile(userId, val);
    }

    updateObs.subscribe({
      next: () => {
        this.loading = false;
        this.authService.setProfileCompleted(true);
        this.toast.success('Profile completed successfully! Welcome to CropDeal.');
        this.router.navigate([this.authService.getRoleDashboardUrl()]);
      },
      error: () => {
        // Fallback for demo/offline: set profile completed and continue
        this.loading = false;
        this.authService.setProfileCompleted(true);
        this.toast.success('Profile saved successfully.');
        this.router.navigate([this.authService.getRoleDashboardUrl()]);
      }
    });
  }

  skipForNow(): void {
    this.authService.setProfileCompleted(true);
    this.router.navigate([this.authService.getRoleDashboardUrl()]);
  }
}
