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
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-add-crop',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, CurrencyInrPipe],
  template: `
    <div class="add-crop-page py-8">
      <div class="container max-w-2xl">
        
        <div class="card p-8">
          
          <div class="mb-6">
            <span class="badge badge-green mb-1">Farmer Listing Studio</span>
            <h1 class="text-2xl font-extrabold text-dark">Publish New Crop Harvest</h1>
            <p class="text-xs text-muted mt-1">List your farm produce directly to dealers with live Government Mandi rate validation.</p>
          </div>

          <form [formGroup]="cropForm" (ngSubmit)="onSubmit()">
            
            <!-- Crop Name & Category -->
            <div class="grid grid-cols-2 gap-4">
              <div class="form-group">
                <label class="form-label">Crop Name *</label>
                <input 
                  type="text" 
                  formControlName="cropName"
                  (blur)="checkGovRates()"
                  placeholder="e.g. Tomato, Onion, Wheat"
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

            <!-- Variety, Grade, Quality & Organic -->
            <div class="grid grid-cols-3 gap-3">
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
                <label class="form-label">Quality</label>
                <select formControlName="quality" class="form-control">
                  <option value="PREMIUM">PREMIUM</option>
                  <option value="GRADE_A">GRADE A</option>
                  <option value="GRADE_B">GRADE B</option>
                  <option value="FAQ">FAQ</option>
                </select>
              </div>
            </div>

            <!-- Quantity & Price -->
            <div class="grid grid-cols-2 gap-4">
              <div class="form-group">
                <label class="form-label">Total Quantity (KG) *</label>
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
            <div *ngIf="govRateFeedback" class="gov-guide-box p-4 rounded-lg mb-4" [class.valid]="govRateFeedback.valid" [class.invalid]="!govRateFeedback.valid">
              <div class="flex items-center gap-2 font-bold text-xs text-dark">
                <span class="material-symbols-outlined text-sm text-emerald-700">insights</span>
                <span>Government Mandi Reference: {{ govRateFeedback.referencePrice | inr }}/KG</span>
              </div>
              <div class="text-2xs text-muted mt-1">
                {{ govRateFeedback.message }} (Allowed Range: {{ govRateFeedback.minAllowedPrice | inr }} - {{ govRateFeedback.maxAllowedPrice | inr }}/KG)
              </div>
            </div>

            <!-- State, District, Location -->
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
                  placeholder="Salem"
                  class="form-control"
                />
              </div>
            </div>

            <div class="form-group">
              <label class="form-label">Farm / Mandi Pickup Location</label>
              <input 
                type="text" 
                formControlName="location"
                placeholder="Village, Post Office, Nearest APMC Landmark"
                class="form-control"
              />
            </div>

            <!-- Organic Switch -->
            <div class="flex items-center gap-2 mb-4">
              <input type="checkbox" formControlName="organic" id="orgSwitch" class="w-4 h-4 text-emerald-600 rounded">
              <label for="orgSwitch" class="text-xs font-bold text-dark cursor-pointer">
                100% Certified Organic Produce (No synthetic pesticides)
              </label>
            </div>

            <!-- Description -->
            <div class="form-group">
              <label class="form-label">Produce Description</label>
              <textarea 
                formControlName="description"
                rows="3"
                placeholder="Mention harvesting date, moisture condition, packaging details..."
                class="form-control"
              ></textarea>
            </div>

            <button 
              type="submit" 
              class="btn btn-primary btn-lg w-full mt-4"
              [disabled]="cropForm.invalid || isSubmitting"
            >
              {{ isSubmitting ? 'Publishing Crop Listing...' : 'Publish Crop to Marketplace' }}
            </button>

          </form>

        </div>

      </div>
    </div>
  `,
  styles: [`
    .gov-guide-box {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
    }
    .gov-guide-box.invalid {
      background: #fef2f2;
      border-color: #fecaca;
    }
    .text-2xs { font-size: 0.625rem; }
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

  cropForm = this.fb.group({
    cropName: ['Tomato', [Validators.required]],
    category: ['VEGETABLES' as CropCategory, [Validators.required]],
    variety: ['Deshi'],
    grade: ['A'],
    quality: ['GRADE_A' as QualityGrade],
    quantityKg: [500, [Validators.required, Validators.min(10)]],
    pricePerKg: [26.00, [Validators.required, Validators.min(1)]],
    state: ['Tamil Nadu', [Validators.required]],
    district: ['Salem', [Validators.required]],
    location: ['Salem Rural Farm Cluster'],
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
