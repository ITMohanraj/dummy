import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CropService } from '../../../core/services/crop.service';
import { PriceService } from '../../../core/services/price.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { CropCategory, QualityGrade } from '../../../core/models/crop.models';
import { PriceValidationResponse } from '../../../core/models/price.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-add-crop',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, SidebarComponent, CurrencyInrPipe],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <!-- Page Header -->
        <div class="page-header mb-6">
          <h1 class="page-title">List New Crop Harvest</h1>
          <p class="page-subtitle">Publish fresh harvest directly to registered buyers with government Mandi price benchmarking</p>
        </div>

        <div class="form-container card p-8">
          <form [formGroup]="cropForm" (ngSubmit)="onSubmit()">
            
            <!-- Section 1: Crop Classification -->
            <div class="form-section mb-6 pb-6 border-b">
              <h3 class="section-heading mb-4">1. Crop Identity & Classification</h3>
              <div class="grid grid-cols-2 gap-4">
                <div class="form-group">
                  <label class="form-label">Crop Name *</label>
                  <input 
                    type="text" 
                    formControlName="cropName"
                    (blur)="checkGovRates()"
                    placeholder="e.g. Tomato, Onion, Turmeric, Wheat"
                    class="form-control"
                  />
                </div>

                <div class="form-group">
                  <label class="form-label">Category *</label>
                  <select formControlName="category" class="form-control">
                    <option value="VEGETABLES">Vegetables</option>
                    <option value="FRUITS">Fruits</option>
                    <option value="CEREALS">Cereals & Grains</option>
                    <option value="PULSES">Pulses</option>
                    <option value="SPICES">Spices</option>
                    <option value="OILSEEDS">Oilseeds</option>
                    <option value="COMMERCIAL">Commercial / Cash Crop</option>
                  </select>
                </div>
              </div>

              <div class="grid grid-cols-3 gap-3 mt-4">
                <div class="form-group">
                  <label class="form-label">Variety</label>
                  <input type="text" formControlName="variety" placeholder="e.g. Deshi / Hybrid" class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label">Grade</label>
                  <select formControlName="grade" class="form-control">
                    <option value="A">Grade A (Premium)</option>
                    <option value="B">Grade B (Standard)</option>
                    <option value="C">Grade C (Commercial)</option>
                    <option value="FAQ">FAQ Standard</option>
                  </select>
                </div>
                <div class="form-group">
                  <label class="form-label">Quality Certification</label>
                  <select formControlName="quality" class="form-control">
                    <option value="PREMIUM">PREMIUM</option>
                    <option value="GRADE_A">GRADE A</option>
                    <option value="GRADE_B">GRADE B</option>
                    <option value="FAQ">FAQ</option>
                  </select>
                </div>
              </div>
            </div>

            <!-- Section 2: Quantity & Pricing -->
            <div class="form-section mb-6 pb-6 border-b">
              <h3 class="section-heading mb-4">2. Harvest Volume & Pricing</h3>
              <div class="grid grid-cols-2 gap-4">
                <div class="form-group">
                  <label class="form-label">Available Quantity (KG) *</label>
                  <input 
                    type="number" 
                    formControlName="quantityKg"
                    placeholder="e.g. 500"
                    class="form-control"
                  />
                </div>

                <div class="form-group">
                  <label class="form-label">Expected Price per KG (₹) *</label>
                  <input 
                    type="number" 
                    formControlName="pricePerKg"
                    (input)="checkGovRates()"
                    placeholder="e.g. 26.50"
                    class="form-control"
                  />
                </div>
              </div>

              <!-- Live Government Rate Validator Alert -->
              <div *ngIf="govRateFeedback" class="gov-guide-box p-4 rounded-lg mt-4" [class.valid]="govRateFeedback.valid" [class.invalid]="!govRateFeedback.valid">
                <div class="flex items-center gap-2 font-bold text-xs text-dark">
                  <span class="material-symbols-outlined text-sm text-emerald-700">insights</span>
                  <span>Government Mandi Reference: {{ govRateFeedback.referencePrice | inr }}/KG</span>
                </div>
                <div class="text-xs text-muted mt-1">
                  {{ govRateFeedback.message }} (Allowed Range: {{ govRateFeedback.minAllowedPrice | inr }} - {{ govRateFeedback.maxAllowedPrice | inr }}/KG)
                </div>
              </div>
            </div>

            <!-- Section 3: Farm Dispatch Location -->
            <div class="form-section mb-6 pb-6 border-b">
              <h3 class="section-heading mb-4">3. Farm Origin Location</h3>
              <div class="grid grid-cols-2 gap-4">
                <div class="form-group">
                  <label class="form-label">State *</label>
                  <input 
                    type="text" 
                    formControlName="state"
                    (blur)="checkGovRates()"
                    placeholder="Tamil Nadu"
                    class="form-control"
                  />
                </div>

                <div class="form-group">
                  <label class="form-label">District *</label>
                  <input 
                    type="text" 
                    formControlName="district"
                    (blur)="checkGovRates()"
                    placeholder="Erode"
                    class="form-control"
                  />
                </div>
              </div>

              <div class="form-group mt-3">
                <label class="form-label">Farm / Mandi Pickup Landmark</label>
                <input 
                  type="text" 
                  formControlName="location"
                  placeholder="Village, Post Office, Nearest Mandi Landmark"
                  class="form-control"
                />
              </div>

              <!-- Organic Switch -->
              <div class="flex items-center gap-2 mt-4">
                <input type="checkbox" formControlName="organic" id="orgSwitch" class="checkbox">
                <label for="orgSwitch" class="text-sm font-bold text-dark cursor-pointer">
                  100% Certified Organic Harvest (Chemical/Pesticide Free)
                </label>
              </div>
            </div>

            <!-- Section 4: Description -->
            <div class="form-section mb-6">
              <h3 class="section-heading mb-4">4. Additional Notes & Packaging</h3>
              <div class="form-group">
                <label class="form-label">Produce Notes</label>
                <textarea 
                  formControlName="description"
                  rows="3"
                  placeholder="Mention harvest timing, moisture percentage, packaging crates..."
                  class="form-control"
                ></textarea>
              </div>
            </div>

            <div class="flex justify-end gap-3 pt-4 border-t">
              <a routerLink="/farmer/crops" class="btn btn-secondary">Cancel</a>
              <button 
                type="submit" 
                class="btn btn-primary btn-lg"
                [disabled]="cropForm.invalid || isSubmitting"
              >
                {{ isSubmitting ? 'Publishing...' : 'Publish Crop to Marketplace' }}
              </button>
            </div>

          </form>
        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-layout { display: flex; min-height: calc(100vh - 120px); background: var(--bg-main); }
    .dashboard-main { flex: 1; padding: 2rem 2.5rem; max-width: 1000px; }
    .page-header { margin-bottom: 1.5rem; }
    .page-title { font-size: 1.625rem; font-weight: 800; color: var(--dark); }
    .page-subtitle { font-size: 0.875rem; color: var(--text-muted); margin-top: 0.25rem; }
    .section-heading { font-size: 1.0625rem; font-weight: 700; color: var(--dark); }
    .border-b { border-bottom: 1px solid var(--border-light); }
    .border-t { border-top: 1px solid var(--border-light); }
    .gov-guide-box { background: #f0fdf4; border: 1px solid #bbf7d0; }
    .gov-guide-box.invalid { background: #fef2f2; border-color: #fecaca; }
    @media (max-width: 900px) {
      .grid-cols-2, .grid-cols-3 { grid-template-columns: 1fr; }
      .dashboard-main { padding: 1.25rem; }
    }
  `]
})
export class AddCropComponent {
  private fb = inject(FormBuilder);
  private cropService = inject(CropService);
  private priceService = inject(PriceService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);
  private router = inject(Router);

  isSubmitting = false;
  govRateFeedback?: PriceValidationResponse;

  sidebarSections: NavSection[] = [
    {
      title: 'Overview',
      items: [{ label: 'Dashboard', icon: 'dashboard', route: '/farmer/dashboard', exact: true }]
    },
    {
      title: 'Crops Management',
      items: [
        { label: 'My Crops', icon: 'inventory_2', route: '/farmer/crops' },
        { label: 'Add New Crop', icon: 'add_circle', route: '/farmer/crops/new', exact: true }
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
        { label: 'Profile Settings', icon: 'person', route: '/profile' },
        { label: 'Notifications', icon: 'notifications', route: '/notifications' }
      ]
    }
  ];

  cropForm = this.fb.group({
    cropName: ['Tomato', [Validators.required]],
    category: ['VEGETABLES' as CropCategory, [Validators.required]],
    variety: ['Deshi'],
    grade: ['A'],
    quality: ['GRADE_A' as QualityGrade],
    quantityKg: [500, [Validators.required, Validators.min(10)]],
    pricePerKg: [26.00, [Validators.required, Validators.min(1)]],
    state: ['Tamil Nadu', [Validators.required]],
    district: ['Erode', [Validators.required]],
    location: ['Bhavani Farm Cluster'],
    organic: [true],
    description: ['Harvested fresh this morning. Graded and packed in ventilated crates.']
  });

  checkGovRates(): void {
    const name = this.cropForm.value.cropName;
    const price = this.cropForm.value.pricePerKg;
    const state = this.cropForm.value.state;
    const district = this.cropForm.value.district;

    if (name && price && price > 0) {
      this.priceService.validateFarmerPrice(name, price, state || undefined, district || undefined).subscribe({
        next: (res) => this.govRateFeedback = res,
        error: () => {}
      });
    }
  }

  onSubmit(): void {
    if (this.cropForm.invalid) return;

    const farmerId = this.authService.getUserId() || 101;
    this.isSubmitting = true;

    this.cropService.createCrop({
      farmerId: farmerId,
      cropName: this.cropForm.value.cropName!,
      category: this.cropForm.value.category as CropCategory,
      variety: this.cropForm.value.variety || undefined,
      grade: this.cropForm.value.grade || undefined,
      quality: this.cropForm.value.quality as QualityGrade,
      quantityKg: this.cropForm.value.quantityKg!,
      pricePerKg: this.cropForm.value.pricePerKg!,
      state: this.cropForm.value.state!,
      district: this.cropForm.value.district!,
      location: this.cropForm.value.location || undefined,
      organic: this.cropForm.value.organic || false,
      description: this.cropForm.value.description || undefined
    }).subscribe({
      next: (res) => {
        this.isSubmitting = false;
        this.toast.success(`Crop listing for "${res.cropName}" published successfully!`);
        this.router.navigate(['/farmer/crops']);
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }
}
