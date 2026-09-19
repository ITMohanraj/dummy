import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PriceService } from '../../core/services/price.service';
import { MandiPriceRecord, PriceValidationResponse } from '../../core/models/price.models';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { ToastService } from '../../core/services/toast.service';

@Component({
  selector: 'app-market-prices',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule, 
    LoadingSpinnerComponent, 
    EmptyStateComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="mandi-portal py-8">
      <div class="container">
        
        <!-- Header & Sync Status Banner -->
        <div class="portal-header card p-6 mb-8">
          <div class="flex justify-between items-start flex-wrap gap-4">
            <div>
              <div class="flex items-center gap-2 mb-2">
                <span class="badge badge-green">Official Agmarknet Integration</span>
                <span class="text-xs text-muted">Resource: data.gov.in/resource/9ef84268</span>
              </div>
              <h1 class="portal-title">Daily Government Mandi Market Prices</h1>
              <p class="text-muted text-sm mt-1">
                Real-time official APMC wholesale arrivals, minimum, maximum, and modal market rates across India.
              </p>
            </div>

            <button 
              class="btn btn-secondary btn-sm"
              [disabled]="isSyncing"
              (click)="triggerGovSync()"
            >
              <span class="material-symbols-outlined text-sm" [class.animate-spin]="isSyncing">sync</span>
              {{ isSyncing ? 'Syncing data.gov.in...' : 'Sync Government Portal' }}
            </button>
          </div>
        </div>

        <!-- Interactive Price Validation Sandbox -->
        <div class="validator-card card p-6 mb-8">
          <h2 class="section-title text-base mb-2">🧪 Live Mandi Price Validation Sandbox</h2>
          <p class="text-xs text-muted mb-4">
            Test and validate your harvest price against the government's official APMC tolerance window (±15%).
          </p>

          <div class="val-form-grid">
            <div class="form-group">
              <label class="form-label">Commodity</label>
              <input type="text" [(ngModel)]="valCropName" placeholder="e.g. Tomato, Onion" class="form-control" />
            </div>

            <div class="form-group">
              <label class="form-label">State</label>
              <input type="text" [(ngModel)]="valState" placeholder="e.g. Tamil Nadu" class="form-control" />
            </div>

            <div class="form-group">
              <label class="form-label">District</label>
              <input type="text" [(ngModel)]="valDistrict" placeholder="e.g. Salem" class="form-control" />
            </div>

            <div class="form-group">
              <label class="form-label">Your Price (₹/KG)</label>
              <input type="number" [(ngModel)]="valPricePerKg" placeholder="25.00" class="form-control" />
            </div>

            <div class="val-btn-wrap">
              <button class="btn btn-primary w-full" (click)="validatePrice()">
                Validate Rate
              </button>
            </div>
          </div>

          <!-- Validation Result Alert -->
          <div *ngIf="validationResult" class="val-result-box mt-4 p-4 rounded-lg" [class.valid]="validationResult.valid" [class.invalid]="!validationResult.valid">
            <div class="flex items-center gap-2 font-bold text-sm">
              <span class="material-symbols-outlined">
                {{ validationResult.valid ? 'check_circle' : 'warning' }}
              </span>
              <span>{{ validationResult.message }}</span>
            </div>
            <div class="text-xs mt-2 text-slate-700 flex gap-6 flex-wrap">
              <span>Gov Modal Reference: <strong>{{ validationResult.referencePrice | inr }}/KG</strong></span>
              <span>Min Allowed: <strong>{{ validationResult.minAllowedPrice | inr }}/KG</strong></span>
              <span>Max Allowed: <strong>{{ validationResult.maxAllowedPrice | inr }}/KG</strong></span>
            </div>
          </div>
        </div>

        <!-- Filter & Search Controls -->
        <div class="table-controls card p-4 mb-6">
          <div class="flex gap-4 items-center flex-wrap">
            <div class="flex-1 min-w-xs">
              <input 
                type="text" 
                [(ngModel)]="searchCommodity" 
                (ngModelChange)="filterRecords()"
                placeholder="Search commodity (e.g. Tomato, Onion, Potato, Bajra)..." 
                class="form-control"
              />
            </div>
            <div class="min-w-xs">
              <select [(ngModel)]="selectedState" (ngModelChange)="filterRecords()" class="form-control">
                <option value="">All States (28 States)</option>
                <option *ngFor="let s of availableStates" [value]="s">{{ s }}</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Mandi Rates Table -->
        <app-loading-spinner *ngIf="loading" message="Loading live Mandi database records..."></app-loading-spinner>

        <div *ngIf="!loading && filteredRecords.length > 0" class="table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>State & District</th>
                <th>Mandi / APMC Market</th>
                <th>Commodity</th>
                <th>Variety & Grade</th>
                <th>Min (₹/Q)</th>
                <th>Max (₹/Q)</th>
                <th>Modal Price (₹/Q)</th>
                <th>Price per KG</th>
                <th>Arrival Date</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let row of filteredRecords.slice(0, 100)">
                <td>
                  <div class="font-bold text-dark">{{ row.state }}</div>
                  <div class="text-xs text-muted">{{ row.district }}</div>
                </td>
                <td>{{ row.market }}</td>
                <td>
                  <span class="font-bold text-emerald-800">{{ row.commodity }}</span>
                </td>
                <td>
                  <span class="text-xs">{{ row.variety || 'Standard' }}</span>
                  <span *ngIf="row.grade" class="badge badge-gray text-2xs ml-1">{{ row.grade }}</span>
                </td>
                <td>{{ row.minPrice | inr }}</td>
                <td>{{ row.maxPrice | inr }}</td>
                <td class="font-bold text-dark">{{ row.modalPrice | inr }}</td>
                <td>
                  <span class="badge badge-green font-bold text-xs">{{ row.convertedPricePerKg | inr }}/KG</span>
                </td>
                <td class="text-xs text-muted">{{ row.recordDate }}</td>
              </tr>
            </tbody>
          </table>

          <div class="p-4 bg-slate-50 border-t border-slate-200 text-xs text-muted flex justify-between">
            <span>Showing top {{ Math.min(100, filteredRecords.length) }} of {{ filteredRecords.length }} matched records</span>
            <span>Prices standard unit: Quintal (100 KG)</span>
          </div>
        </div>

        <app-empty-state 
          *ngIf="!loading && filteredRecords.length === 0"
          icon="manage_search"
          title="No Mandi Records Found"
          description="Try searching for a different commodity like Tomato, Onion, Potato, or Wheat."
        ></app-empty-state>

      </div>
    </div>
  `,
  styles: [`
    .portal-title {
      font-size: 1.875rem;
      font-weight: 800;
      color: var(--dark);
    }
    .val-form-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)) 140px;
      gap: 1rem;
      align-items: flex-end;
    }
    .val-result-box {
      border: 1px solid var(--border-light);
    }
    .val-result-box.valid {
      background: #f0fdf4;
      border-color: #bbf7d0;
      color: #166534;
    }
    .val-result-box.invalid {
      background: #fef2f2;
      border-color: #fecaca;
      color: #991b1b;
    }
    .text-2xs { font-size: 0.625rem; }
    .animate-spin {
      animation: spin 1s linear infinite;
    }
    @keyframes spin {
      to { transform: rotate(360deg); }
    }
    @media (max-width: 1024px) {
      .val-form-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class MarketPricesComponent implements OnInit {
  private priceService = inject(PriceService);
  private toast = inject(ToastService);

  Math = Math;
  loading = true;
  isSyncing = false;

  allRecords: MandiPriceRecord[] = [];
  filteredRecords: MandiPriceRecord[] = [];
  availableStates: string[] = [];

  searchCommodity = '';
  selectedState = '';

  valCropName = 'Tomato';
  valState = 'Tamil Nadu';
  valDistrict = 'Salem';
  valPricePerKg = 25.00;
  validationResult?: PriceValidationResponse;

  ngOnInit(): void {
    this.loadRecords();
  }

  loadRecords(): void {
    this.loading = true;
    this.priceService.getMandiRates().subscribe({
      next: (res) => {
        this.allRecords = res || [];
        this.availableStates = Array.from(new Set(res.map(r => r.state))).sort();
        this.filterRecords();
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.toast.error('Failed to load Mandi records.');
      }
    });
  }

  filterRecords(): void {
    this.filteredRecords = this.allRecords.filter(r => {
      const matchComm = !this.searchCommodity || 
        r.commodity.toLowerCase().includes(this.searchCommodity.toLowerCase());
      const matchState = !this.selectedState || r.state === this.selectedState;
      return matchComm && matchState;
    });
  }

  validatePrice(): void {
    if (!this.valCropName || !this.valPricePerKg) {
      this.toast.warning('Please enter a crop name and price per KG to validate.');
      return;
    }

    this.priceService.validateFarmerPrice(
      this.valCropName,
      this.valPricePerKg,
      this.valState,
      this.valDistrict
    ).subscribe({
      next: (res) => {
        this.validationResult = res;
      },
      error: () => {
        this.toast.error('Validation request failed.');
      }
    });
  }

  triggerGovSync(): void {
    this.isSyncing = true;
    this.priceService.syncGovernmentPrices().subscribe({
      next: (res) => {
        this.isSyncing = false;
        this.toast.success(`Government Mandi Sync Complete: ${res.recordsSynced} records updated.`);
        this.loadRecords();
      },
      error: () => {
        this.isSyncing = false;
        this.toast.error('Government portal sync encountered a transient rate limit.');
      }
    });
  }
}
