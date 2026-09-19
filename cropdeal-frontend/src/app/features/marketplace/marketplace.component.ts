import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CropService } from '../../core/services/crop.service';
import { CropCategory, CropResponse } from '../../core/models/crop.models';
import { CropCardComponent } from '../../shared/components/crop-card/crop-card.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-marketplace',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule, 
    CropCardComponent, 
    LoadingSpinnerComponent, 
    EmptyStateComponent
  ],
  template: `
    <div class="marketplace-page py-8">
      <div class="container">
        
        <!-- Marketplace Breadcrumb & Header -->
        <div class="marketplace-header mb-6">
          <div class="flex items-center gap-2 text-xs text-muted mb-2">
            <a routerLink="/">Home</a>
            <span>/</span>
            <span class="text-dark font-semibold">Crops Marketplace</span>
          </div>
          <div class="flex justify-between items-end flex-wrap gap-4">
            <div>
              <h1 class="page-title">Live Agricultural Marketplace</h1>
              <p class="text-muted text-sm">Discover verified crop listings directly from farmers across India</p>
            </div>
            <div class="text-sm font-semibold text-emerald-800 bg-emerald-50 border border-emerald-200 px-3 py-1.5 rounded-full">
              Showing {{ totalElements }} Active Listings
            </div>
          </div>
        </div>

        <!-- Filter Bar & Search -->
        <div class="filters-card card p-4 mb-6">
          <div class="filter-row">
            
            <!-- Search Keyword -->
            <div class="filter-input-wrap search-box">
              <span class="material-symbols-outlined text-muted">search</span>
              <input 
                type="text" 
                [(ngModel)]="searchName" 
                (ngModelChange)="onFilterChange()"
                placeholder="Search crop name (Tomato, Wheat, Onion)..."
                class="form-control"
              />
            </div>

            <!-- Category Filter -->
            <div class="filter-input-wrap">
              <select [(ngModel)]="selectedCategory" (ngModelChange)="onFilterChange()" class="form-control">
                <option value="">All Categories</option>
                <option value="VEGETABLES">Vegetables</option>
                <option value="FRUITS">Fruits</option>
                <option value="CEREALS">Cereals & Grains</option>
                <option value="PULSES">Pulses</option>
                <option value="SPICES">Spices</option>
                <option value="OILSEEDS">Oilseeds</option>
              </select>
            </div>

            <!-- State Filter -->
            <div class="filter-input-wrap">
              <input 
                type="text" 
                [(ngModel)]="searchState" 
                (ngModelChange)="onFilterChange()"
                placeholder="State (e.g. Tamil Nadu, Punjab)..."
                class="form-control"
              />
            </div>

            <!-- District Filter -->
            <div class="filter-input-wrap">
              <input 
                type="text" 
                [(ngModel)]="searchDistrict" 
                (ngModelChange)="onFilterChange()"
                placeholder="District..."
                class="form-control"
              />
            </div>

            <!-- Organic Only Toggle -->
            <div class="filter-toggle flex items-center gap-2">
              <label class="toggle-switch">
                <input type="checkbox" [(ngModel)]="organicOnly" (ngModelChange)="onFilterChange()">
                <span class="slider"></span>
              </label>
              <span class="text-xs font-bold text-dark">Organic Only</span>
            </div>

            <!-- Reset Button -->
            <button class="btn btn-secondary btn-sm" (click)="resetFilters()">
              <span class="material-symbols-outlined text-xs">restart_alt</span>
              Reset
            </button>

          </div>
        </div>

        <!-- Main Marketplace Content -->
        <app-loading-spinner *ngIf="loading" message="Filtering verified crop listings..."></app-loading-spinner>

        <div *ngIf="!loading && crops.length > 0">
          <div class="crops-grid">
            <app-crop-card *ngFor="let crop of crops" [crop]="crop"></app-crop-card>
          </div>

          <!-- Pagination -->
          <div class="pagination-wrap flex justify-between items-center mt-8 p-4 bg-white rounded-lg border border-slate-200">
            <div class="text-xs text-muted">
              Page {{ currentPage + 1 }} of {{ totalPages || 1 }} ({{ totalElements }} total crops)
            </div>
            <div class="flex gap-2">
              <button 
                class="btn btn-secondary btn-sm" 
                [disabled]="currentPage === 0"
                (click)="goToPage(currentPage - 1)"
              >
                Previous
              </button>
              <button 
                class="btn btn-secondary btn-sm" 
                [disabled]="currentPage >= totalPages - 1"
                (click)="goToPage(currentPage + 1)"
              >
                Next
              </button>
            </div>
          </div>
        </div>

        <app-empty-state 
          *ngIf="!loading && crops.length === 0"
          icon="search_off"
          title="No Crops Found Matching Your Filters"
          description="Try broadening your search term, selecting 'All Categories', or clearing state/district filters."
          actionLabel="Reset All Filters"
          (actionClicked)="resetFilters()"
        ></app-empty-state>

      </div>
    </div>
  `,
  styles: [`
    .page-title {
      font-size: 1.75rem;
      font-weight: 800;
      color: var(--dark);
    }
    .filters-card {
      background: #ffffff;
    }
    .filter-row {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.75rem;
    }
    .filter-input-wrap {
      flex: 1;
      min-width: 160px;
    }
    .search-box {
      position: relative;
      flex: 1.5;
      min-width: 220px;
    }
    .search-box .material-symbols-outlined {
      position: absolute;
      left: 0.75rem;
      top: 50%;
      transform: translateY(-50%);
      pointer-events: none;
    }
    .search-box input {
      padding-left: 2.25rem;
    }
    .crops-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1.5rem;
    }
    .toggle-switch {
      position: relative;
      display: inline-block;
      width: 36px;
      height: 20px;
    }
    .toggle-switch input { opacity: 0; width: 0; height: 0; }
    .slider {
      position: absolute;
      cursor: pointer;
      top: 0; left: 0; right: 0; bottom: 0;
      background-color: #cbd5e1;
      transition: .3s;
      border-radius: 20px;
    }
    .slider:before {
      position: absolute;
      content: "";
      height: 14px;
      width: 14px;
      left: 3px;
      bottom: 3px;
      background-color: white;
      transition: .3s;
      border-radius: 50%;
    }
    input:checked + .slider {
      background-color: var(--primary);
    }
    input:checked + .slider:before {
      transform: translateX(16px);
    }
    .py-8 { padding-top: 2rem; padding-bottom: 2rem; }
  `]
})
export class MarketplaceComponent implements OnInit {
  private cropService = inject(CropService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  searchName = '';
  selectedCategory: string = '';
  searchState = '';
  searchDistrict = '';
  organicOnly = false;

  currentPage = 0;
  pageSize = 12;
  totalPages = 0;
  totalElements = 0;

  loading = true;
  crops: CropResponse[] = [];

  ngOnInit(): void {
    this.route.queryParams.subscribe(params => {
      if (params['cropName']) this.searchName = params['cropName'];
      if (params['category']) this.selectedCategory = params['category'];
      if (params['state']) this.searchState = params['state'];
      if (params['district']) this.searchDistrict = params['district'];
      if (params['organic']) this.organicOnly = params['organic'] === 'true';

      this.fetchCrops();
    });
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.fetchCrops();
  }

  fetchCrops(): void {
    this.loading = true;

    this.cropService.searchCrops({
      cropName: this.searchName || undefined,
      category: (this.selectedCategory as CropCategory) || undefined,
      state: this.searchState || undefined,
      district: this.searchDistrict || undefined,
      organic: this.organicOnly ? true : undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (res) => {
        this.crops = res.content || [];
        this.totalPages = res.totalPages;
        this.totalElements = res.totalElements;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  goToPage(page: number): void {
    this.currentPage = page;
    this.fetchCrops();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  resetFilters(): void {
    this.searchName = '';
    this.selectedCategory = '';
    this.searchState = '';
    this.searchDistrict = '';
    this.organicOnly = false;
    this.currentPage = 0;
    this.fetchCrops();
  }
}
