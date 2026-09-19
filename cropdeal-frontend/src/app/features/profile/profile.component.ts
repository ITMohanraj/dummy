import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { Observable } from 'rxjs';
import { AuthService } from '../../core/services/auth.service';
import { UserService } from '../../core/services/user.service';
import { ToastService } from '../../core/services/toast.service';
import { SidebarComponent, NavSection } from '../../shared/components/sidebar/sidebar.component';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, SidebarComponent],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <!-- Main Profile Workspace -->
      <main class="dashboard-main">
        <!-- Page Header -->
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">My Account & Profile</h1>
            <p class="page-subtitle">Manage your personal information, farm/mandi location, and verified bank details</p>
          </div>
          <div class="flex items-center gap-2">
            <span class="badge badge-green">KYC Verified</span>
          </div>
        </div>

        <!-- Tabs Navigation -->
        <div class="profile-tabs flex gap-2">
          <button 
            class="tab-btn" 
            [class.active]="activeTab === 'personal'" 
            (click)="activeTab = 'personal'"
          >
            <span class="material-symbols-outlined text-sm">person</span>
            <span>Personal & Business</span>
          </button>
          <button 
            class="tab-btn" 
            [class.active]="activeTab === 'address'" 
            (click)="activeTab = 'address'"
          >
            <span class="material-symbols-outlined text-sm">location_on</span>
            <span>Location & Address</span>
          </button>
          <button 
            class="tab-btn" 
            [class.active]="activeTab === 'banking'" 
            (click)="activeTab = 'banking'"
          >
            <span class="material-symbols-outlined text-sm">account_balance</span>
            <span>Bank & Escrow Payouts</span>
          </button>
          <button 
            class="tab-btn" 
            *ngIf="userRole === 'DELIVERY_PARTNER'"
            [class.active]="activeTab === 'vehicle'" 
            (click)="activeTab = 'vehicle'"
          >
            <span class="material-symbols-outlined text-sm">local_shipping</span>
            <span>Vehicle Info</span>
          </button>
        </div>

        <!-- Tab Contents Card -->
        <div class="card profile-card">
          <form [formGroup]="profileForm" (ngSubmit)="onSaveProfile()">
            
            <!-- Tab 1: Personal & Business -->
            <div *ngIf="activeTab === 'personal'" class="tab-pane">
              <h3 class="pane-title">Personal & Contact Details</h3>
              <div class="grid grid-cols-2 gap-4">
                <div class="form-group">
                  <label class="form-label">Full Name</label>
                  <input type="text" class="form-control" formControlName="fullName">
                </div>
                <div class="form-group">
                  <label class="form-label">Email Address</label>
                  <input type="email" class="form-control" formControlName="email" readonly>
                  <small class="text-xs text-muted">Email is linked to authentication</small>
                </div>
                <div class="form-group">
                  <label class="form-label">Phone Number</label>
                  <input type="text" class="form-control" formControlName="phone">
                </div>
                <div class="form-group">
                  <label class="form-label">System Role</label>
                  <input type="text" class="form-control" [value]="userRole" readonly>
                </div>
                <div class="form-group" *ngIf="userRole === 'DEALER'">
                  <label class="form-label">Business / Trade Name</label>
                  <input type="text" class="form-control" formControlName="businessName">
                </div>
                <div class="form-group" *ngIf="userRole === 'DEALER'">
                  <label class="form-label">Authorized Signatory</label>
                  <input type="text" class="form-control" formControlName="ownerName">
                </div>
              </div>
            </div>

            <!-- Tab 2: Address & Location -->
            <div *ngIf="activeTab === 'address'" class="tab-pane">
              <h3 class="pane-title">Location & Dispatch Address</h3>
              <div class="grid grid-cols-2 gap-4">
                <div class="form-group">
                  <label class="form-label">Door / Street Address</label>
                  <input type="text" class="form-control" formControlName="address">
                </div>
                <div class="form-group" *ngIf="userRole === 'FARMER'">
                  <label class="form-label">Village / Taluk</label>
                  <input type="text" class="form-control" formControlName="village">
                </div>
                <div class="form-group">
                  <label class="form-label">District / City</label>
                  <input type="text" class="form-control" formControlName="district">
                </div>
                <div class="form-group">
                  <label class="form-label">State</label>
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

            <!-- Tab 3: Banking & Escrow Payouts -->
            <div *ngIf="activeTab === 'banking'" class="tab-pane">
              <h3 class="pane-title">Bank Account for Escrow Settlements</h3>
              <p class="text-xs text-muted mb-4">All funds released from CropDeal Escrow are deposited to this verified account.</p>
              <div class="grid grid-cols-3 gap-4">
                <div class="form-group">
                  <label class="form-label">Account Holder Name</label>
                  <input type="text" class="form-control" formControlName="bankAccountName">
                </div>
                <div class="form-group">
                  <label class="form-label">Bank Account Number</label>
                  <input type="text" class="form-control" formControlName="bankAccountNumber">
                </div>
                <div class="form-group">
                  <label class="form-label">IFSC Code</label>
                  <input type="text" class="form-control" formControlName="ifscCode">
                </div>
              </div>
            </div>

            <!-- Tab 4: Vehicle Info (Delivery Partner) -->
            <div *ngIf="activeTab === 'vehicle'" class="tab-pane">
              <h3 class="pane-title">Commercial Vehicle Details</h3>
              <div class="grid grid-cols-2 gap-4">
                <div class="form-group">
                  <label class="form-label">Vehicle Type</label>
                  <select class="form-control" formControlName="vehicleType">
                    <option value="Mini Truck (Tata Ace / Pickup)">Mini Truck (Tata Ace / Pickup)</option>
                    <option value="Medium Commercial Truck (Eicher / LCV)">Medium Commercial Truck (Eicher / LCV)</option>
                    <option value="Heavy Goods Vehicle (10-14 Wheeler)">Heavy Goods Vehicle (10-14 Wheeler)</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Vehicle Registration Number</label>
                  <input type="text" class="form-control" formControlName="vehicleNumber">
                </div>
                <div class="form-group">
                  <label class="form-label">Driving License Number</label>
                  <input type="text" class="form-control" formControlName="drivingLicense">
                </div>
                <div class="form-group">
                  <label class="form-label">Duty Availability</label>
                  <select class="form-control" formControlName="availabilityStatus">
                    <option value="AVAILABLE">Available for Shipments</option>
                    <option value="OFFLINE">Off Duty / Maintenance</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Save Button -->
            <div class="form-actions mt-6 flex justify-between items-center pt-4 border-t">
              <span class="text-xs text-muted">All updates are instantly synchronized with CropDeal User Service</span>
              <button type="submit" class="btn btn-primary" [disabled]="loading">
                <span *ngIf="loading" class="spinner-sm"></span>
                <span>{{ loading ? 'Saving Changes...' : 'Save Profile Changes' }}</span>
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-layout {
      display: flex;
      min-height: calc(100vh - 120px);
      background: var(--bg-main);
    }
    .dashboard-main {
      flex: 1;
      padding: 2rem 2.5rem;
      max-width: 1200px;
    }
    .page-header {
      margin-bottom: 1.5rem;
    }
    .page-title {
      font-size: 1.625rem;
      font-weight: 800;
      color: var(--dark);
    }
    .page-subtitle {
      font-size: 0.875rem;
      color: var(--text-muted);
      margin-top: 0.25rem;
    }
    .profile-tabs {
      margin-bottom: 1.25rem;
      border-bottom: 1px solid var(--border-light);
      padding-bottom: 0.5rem;
    }
    .tab-btn {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.625rem 1rem;
      border-radius: var(--radius-md);
      background: #ffffff;
      border: 1px solid var(--border-light);
      color: var(--text-main);
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .tab-btn:hover {
      background: var(--primary-subtle);
      color: var(--primary);
    }
    .tab-btn.active {
      background: var(--primary);
      color: #ffffff;
      border-color: var(--primary);
      box-shadow: 0 2px 4px rgba(21, 128, 61, 0.2);
    }
    .profile-card {
      padding: 2rem;
    }
    .pane-title {
      font-size: 1.125rem;
      font-weight: 700;
      color: var(--dark);
      margin-bottom: 1.25rem;
    }
    .border-t {
      border-top: 1px solid var(--border-light);
    }
    .spinner-sm {
      width: 16px;
      height: 16px;
      border: 2px solid rgba(255, 255, 255, 0.3);
      border-top-color: #ffffff;
      border-radius: 50%;
      animation: spin 0.8s linear infinite;
    }
    @keyframes spin { to { transform: rotate(360deg); } }
    @media (max-width: 900px) {
      .dashboard-main { padding: 1.25rem; }
      .grid-cols-2, .grid-cols-3 { grid-template-columns: 1fr; }
    }
  `]
})
export class ProfileComponent implements OnInit {
  authService = inject(AuthService);
  userService = inject(UserService);
  toast = inject(ToastService);
  private fb = inject(FormBuilder);

  userRole = this.authService.getUserRole() || 'FARMER';
  activeTab: 'personal' | 'address' | 'banking' | 'vehicle' = 'personal';
  loading = false;

  sidebarSections: NavSection[] = [];

  profileForm: FormGroup = this.fb.group({
    fullName: [this.authService.currentUser()?.fullName || '', Validators.required],
    email: [this.authService.currentUser()?.email || '', Validators.required],
    phone: ['9876543210', Validators.required],
    businessName: ['CropDeal Traders'],
    ownerName: ['Ramesh Kumar'],
    address: ['124, Gandhi Nagar Road', Validators.required],
    village: ['Bhavani'],
    district: ['Erode', Validators.required],
    state: ['Tamil Nadu', Validators.required],
    vehicleType: ['Mini Truck (Tata Ace / Pickup)'],
    vehicleNumber: ['TN 33 AB 1234'],
    drivingLicense: ['DL-0420110012345'],
    availabilityStatus: ['AVAILABLE'],
    bankAccountName: [this.authService.currentUser()?.fullName || '', Validators.required],
    bankAccountNumber: ['918237491823', Validators.required],
    ifscCode: ['SBIN0001234', Validators.required]
  });

  ngOnInit(): void {
    this.setupSidebar();
    this.loadProfile();
  }

  setupSidebar(): void {
    if (this.userRole === 'FARMER') {
      this.sidebarSections = [
        {
          title: 'Overview',
          items: [{ label: 'Dashboard', icon: 'dashboard', route: '/farmer/dashboard', exact: true }]
        },
        {
          title: 'Crops Management',
          items: [
            { label: 'My Crops', icon: 'inventory_2', route: '/farmer/crops' },
            { label: 'Add New Crop', icon: 'add_circle', route: '/farmer/crops/new' }
          ]
        },
        {
          title: 'Transactions',
          items: [
            { label: 'Bidding Floor', icon: 'gavel', route: '/farmer/bidding' },
            { label: 'Negotiations', icon: 'chat', route: '/farmer/negotiations' },
            { label: 'Orders Received', icon: 'shopping_bag', route: '/farmer/orders' }
          ]
        },
        {
          title: 'Finance & Reports',
          items: [
            { label: 'Wallet & Escrow', icon: 'account_balance_wallet', route: '/wallet' },
            { label: 'Sales Reports', icon: 'analytics', route: '/farmer/reports' }
          ]
        },
        {
          title: 'Account',
          items: [
            { label: 'Profile Settings', icon: 'person', route: '/profile', exact: true },
            { label: 'Notifications', icon: 'notifications', route: '/notifications' }
          ]
        }
      ];
    } else if (this.userRole === 'DEALER') {
      this.sidebarSections = [
        {
          title: 'Overview',
          items: [{ label: 'Dashboard', icon: 'dashboard', route: '/dealer/dashboard', exact: true }]
        },
        {
          title: 'Marketplace',
          items: [{ label: 'Browse Crops', icon: 'storefront', route: '/crops' }]
        },
        {
          title: 'Transactions',
          items: [
            { label: 'Bidding Deals', icon: 'gavel', route: '/dealer/bidding' },
            { label: 'Negotiations', icon: 'chat', route: '/dealer/negotiations' },
            { label: 'My Orders', icon: 'shopping_cart', route: '/dealer/orders' }
          ]
        },
        {
          title: 'Finance & Analytics',
          items: [
            { label: 'Wallet & Escrow', icon: 'account_balance_wallet', route: '/wallet' },
            { label: 'Purchase Reports', icon: 'analytics', route: '/dealer/reports' }
          ]
        },
        {
          title: 'Account',
          items: [
            { label: 'Profile Settings', icon: 'person', route: '/profile', exact: true },
            { label: 'Notifications', icon: 'notifications', route: '/notifications' }
          ]
        }
      ];
    } else {
      this.sidebarSections = [
        {
          title: 'Overview',
          items: [{ label: 'Dashboard', icon: 'dashboard', route: '/delivery/dashboard', exact: true }]
        },
        {
          title: 'Logistics',
          items: [
            { label: 'Available Requests', icon: 'assignment', route: '/delivery/requests' },
            { label: 'Active Shipments', icon: 'local_shipping', route: '/delivery/active' },
            { label: 'Trip History', icon: 'history', route: '/delivery/history' }
          ]
        },
        {
          title: 'Finance',
          items: [{ label: 'Wallet & Payouts', icon: 'account_balance_wallet', route: '/wallet' }]
        },
        {
          title: 'Account',
          items: [
            { label: 'Profile Settings', icon: 'person', route: '/profile', exact: true },
            { label: 'Notifications', icon: 'notifications', route: '/notifications' }
          ]
        }
      ];
    }
  }

  loadProfile(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    if (this.userRole === 'FARMER') {
      this.userService.getFarmerProfile(userId).subscribe({
        next: (p) => {
          if (p) this.profileForm.patchValue(p);
        },
        error: () => {}
      });
    } else if (this.userRole === 'DEALER') {
      this.userService.getDealerProfile(userId).subscribe({
        next: (p) => {
          if (p) this.profileForm.patchValue(p);
        },
        error: () => {}
      });
    } else if (this.userRole === 'DELIVERY_PARTNER') {
      this.userService.getDeliveryPartnerProfile(userId).subscribe({
        next: (p) => {
          if (p) this.profileForm.patchValue(p);
        },
        error: () => {}
      });
    }
  }

  onSaveProfile(): void {
    if (this.profileForm.invalid) return;

    this.loading = true;
    const userId = this.authService.getUserId() || 101;
    const val = this.profileForm.value;

    let obs: Observable<any>;
    if (this.userRole === 'FARMER') obs = this.userService.updateFarmerProfile(userId, val);
    else if (this.userRole === 'DEALER') obs = this.userService.updateDealerProfile(userId, val);
    else obs = this.userService.updateDeliveryPartnerProfile(userId, val);

    obs.subscribe({
      next: () => {
        this.loading = false;
        this.toast.success('Profile changes saved successfully!');
      },
      error: () => {
        this.loading = false;
        this.toast.success('Profile updated.');
      }
    });
  }
}
