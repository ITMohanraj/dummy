import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CropService } from '../../core/services/crop.service';
import { PriceService } from '../../core/services/price.service';
import { CropResponse, CropCategory } from '../../core/models/crop.models';
import { MandiPriceRecord } from '../../core/models/price.models';
import { CropCardComponent } from '../../shared/components/crop-card/crop-card.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule, 
    CropCardComponent, 
    LoadingSpinnerComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="home-page">
      
      <!-- 1. Hero Section -->
      <section class="hero-section">
        <div class="container">
          <div class="hero-content">
            
            <div class="hero-badge">
              <span class="material-symbols-outlined text-sm">workspace_premium</span>
              <span>India's Direct Farmer-to-Dealer Agricultural Network</span>
            </div>

            <h1 class="hero-title">
              Buy & Sell Fresh Crops <br/>
              <span class="text-gradient">Directly at Verified Mandi Rates</span>
            </h1>

            <p class="hero-subtitle">
              Connect directly with registered farmers and dealers across 28 states. Transparent pricing backed by official Government Mandi daily rates and secure escrow protection.
            </p>

            <!-- Search Form Bar -->
            <div class="hero-search-card">
              <div class="search-grid">
                
                <div class="search-field">
                  <span class="material-symbols-outlined text-muted">search</span>
                  <input 
                    type="text" 
                    [(ngModel)]="searchCropName" 
                    placeholder="Search crops (e.g. Tomato, Onion, Wheat)..."
                    (keyup.enter)="onSearch()"
                  />
                </div>

                <div class="search-divider"></div>

                <div class="search-field">
                  <span class="material-symbols-outlined text-muted">category</span>
                  <select [(ngModel)]="selectedCategory">
                    <option value="">All Categories</option>
                    <option value="VEGETABLES">Vegetables</option>
                    <option value="FRUITS">Fruits</option>
                    <option value="CEREALS">Cereals & Grains</option>
                    <option value="PULSES">Pulses</option>
                    <option value="SPICES">Spices</option>
                  </select>
                </div>

                <div class="search-divider"></div>

                <div class="search-field">
                  <span class="material-symbols-outlined text-muted">location_on</span>
                  <input 
                    type="text" 
                    [(ngModel)]="searchState" 
                    placeholder="State / District..."
                    (keyup.enter)="onSearch()"
                  />
                </div>

                <button class="btn btn-primary search-btn" (click)="onSearch()">
                  <span class="material-symbols-outlined">search</span>
                  Find Crops
                </button>

              </div>
            </div>

            <!-- Hero Quick Stats -->
            <div class="hero-stats flex items-center justify-center gap-8 mt-8">
              <div class="stat-item">
                <div class="stat-val">1,000+</div>
                <div class="stat-lbl">Live Mandi Rates</div>
              </div>
              <div class="stat-sep"></div>
              <div class="stat-item">
                <div class="stat-val">100%</div>
                <div class="stat-lbl">Escrow Protection</div>
              </div>
              <div class="stat-sep"></div>
              <div class="stat-item">
                <div class="stat-val">₹10/km</div>
                <div class="stat-lbl">Direct Farm Logistics</div>
              </div>
            </div>

          </div>
        </div>
      </section>

      <!-- 2. Live Government Mandi Rates Marquee / Ticker -->
      <section class="mandi-ticker-section" *ngIf="mandiRates.length > 0">
        <div class="container">
          <div class="ticker-header flex justify-between items-center mb-3">
            <div class="flex items-center gap-2">
              <span class="material-symbols-outlined text-emerald-600">trending_up</span>
              <h2 class="text-base font-bold">Today's Live Government Mandi Prices (₹/KG)</h2>
            </div>
            <a routerLink="/market-prices" class="text-xs font-bold text-emerald-700 flex items-center gap-1">
              View all 1,000+ Records <span class="material-symbols-outlined text-xs">arrow_forward</span>
            </a>
          </div>

          <div class="ticker-scroll">
            <div *ngFor="let rate of mandiRates.slice(0, 8)" class="ticker-card card">
              <div class="flex justify-between items-center">
                <span class="font-bold text-sm text-dark">{{ rate.commodity }}</span>
                <span class="badge badge-green">{{ rate.convertedPricePerKg | inr }}/KG</span>
              </div>
              <div class="text-xs text-muted mt-1">{{ rate.market }}, {{ rate.district }}</div>
              <div class="text-2xs text-slate-400 mt-0.5">Modal: {{ rate.modalPrice | inr }} / Quintal</div>
            </div>
          </div>
        </div>
      </section>

      <!-- 3. Categories Section -->
      <section class="categories-section py-12">
        <div class="container">
          <div class="section-title-wrap text-center mb-8">
            <h2 class="section-title">Explore by Crop Category</h2>
            <p class="section-sub">Browse fresh harvest listings categorized by agricultural produce</p>
          </div>

          <div class="category-grid">
            <div 
              *ngFor="let cat of categories" 
              class="category-card card"
              (click)="selectCategory(cat.id)"
            >
              <div class="cat-icon-wrap" [style.background]="cat.bg">
                <span class="material-symbols-outlined" [style.color]="cat.color">{{ cat.icon }}</span>
              </div>
              <h3 class="cat-name">{{ cat.name }}</h3>
              <span class="cat-count">{{ cat.desc }}</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 4. Featured Marketplace Crops -->
      <section class="featured-crops-section py-12 bg-white">
        <div class="container">
          <div class="flex justify-between items-end mb-8">
            <div>
              <span class="badge badge-green mb-2">Verified Harvest</span>
              <h2 class="section-title">Popular Crop Listings</h2>
              <p class="section-sub">Direct listings from farmers with guaranteed availability</p>
            </div>
            <a routerLink="/crops" class="btn btn-secondary btn-sm">
              View All Marketplace <span class="material-symbols-outlined text-xs">arrow_forward</span>
            </a>
          </div>

          <app-loading-spinner *ngIf="loadingCrops" message="Loading active crop listings..."></app-loading-spinner>

          <div *ngIf="!loadingCrops && featuredCrops.length > 0" class="crops-grid">
            <app-crop-card *ngFor="let crop of featuredCrops.slice(0, 8)" [crop]="crop"></app-crop-card>
          </div>

          <div *ngIf="!loadingCrops && featuredCrops.length === 0" class="text-center py-12">
            <p class="text-muted">No crop listings published yet. Be the first farmer to list!</p>
            <a routerLink="/farmer/crops/new" class="btn btn-primary mt-4">List Your Crop</a>
          </div>
        </div>
      </section>

      <!-- 5. How CropDeal Works -->
      <section class="how-it-works py-16">
        <div class="container">
          <div class="text-center mb-12">
            <span class="badge badge-gold mb-2">Simple & Transparent</span>
            <h2 class="section-title">How CropDeal Operates</h2>
            <p class="section-sub">Direct agricultural commerce without intermediaries</p>
          </div>

          <div class="steps-grid">
            
            <div class="step-card card">
              <div class="step-num">1</div>
              <div class="step-icon">
                <span class="material-symbols-outlined">agriculture</span>
              </div>
              <h3>Farmer Lists Harvest</h3>
              <p>Farmer specifies crop, quantity, and price. Live Government Mandi ranges assist in setting fair rates.</p>
            </div>

            <div class="step-card card">
              <div class="step-num">2</div>
              <div class="step-icon">
                <span class="material-symbols-outlined">storefront</span>
              </div>
              <h3>Dealer Finds & Purchases</h3>
              <p>Dealers discover crops, negotiate pricing, or place bids on live crop auctions across India.</p>
            </div>

            <div class="step-card card">
              <div class="step-num">3</div>
              <div class="step-icon">
                <span class="material-symbols-outlined">lock</span>
              </div>
              <h3>Funds Held in Escrow</h3>
              <p>Payment is safely held in CropDeal Escrow until the crop is inspected and verified upon arrival.</p>
            </div>

            <div class="step-card card">
              <div class="step-num">4</div>
              <div class="step-icon">
                <span class="material-symbols-outlined">local_shipping</span>
              </div>
              <h3>Transport & Instant Payout</h3>
              <p>Delivery partners transport the harvest at ₹10/km. Funds are released instantly to the farmer's wallet.</p>
            </div>

          </div>
        </div>
      </section>

      <!-- 6. Call to Action Banner -->
      <section class="cta-section">
        <div class="container">
          <div class="cta-card">
            <div class="cta-content">
              <h2>Join Over 50,000+ Farmers & Dealers Today</h2>
              <p>Experience hassle-free direct trade, verified market rates, and instant digital payments.</p>
              <div class="flex gap-4 mt-6 flex-wrap">
                <a routerLink="/register" [queryParams]="{role: 'FARMER'}" class="btn btn-primary btn-lg">
                  Register as Farmer
                </a>
                <a routerLink="/register" [queryParams]="{role: 'DEALER'}" class="btn btn-accent btn-lg">
                  Register as Dealer
                </a>
              </div>
            </div>
          </div>
        </div>
      </section>

    </div>
  `,
  styles: [`
    .hero-section {
      background: linear-gradient(180deg, #f0fdf4 0%, #ffffff 100%);
      padding: 4.5rem 0 3rem;
      text-align: center;
      position: relative;
    }
    .hero-content {
      max-width: 860px;
      margin: 0 auto;
    }
    .hero-badge {
      display: inline-flex;
      align-items: center;
      gap: 0.5rem;
      background: #dcfce7;
      color: #166534;
      border: 1px solid #bbf7d0;
      padding: 0.375rem 1rem;
      border-radius: var(--radius-full);
      font-size: 0.8125rem;
      font-weight: 700;
      margin-bottom: 1.5rem;
    }
    .hero-title {
      font-size: 3rem;
      font-weight: 800;
      letter-spacing: -0.03em;
      margin-bottom: 1.25rem;
      line-height: 1.15;
    }
    .text-gradient {
      background: linear-gradient(135deg, var(--primary-dark) 0%, var(--primary) 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
    }
    .hero-subtitle {
      font-size: 1.125rem;
      color: var(--text-muted);
      margin-bottom: 2.5rem;
      line-height: 1.6;
    }
    .hero-search-card {
      background: #ffffff;
      padding: 0.75rem;
      border-radius: var(--radius-xl);
      box-shadow: var(--shadow-xl);
      border: 1px solid var(--border-light);
    }
    .search-grid {
      display: grid;
      grid-template-columns: 2fr auto 1.5fr auto 1.5fr auto;
      align-items: center;
      gap: 0.75rem;
    }
    .search-field {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 0.75rem;
    }
    .search-field input, .search-field select {
      border: none;
      outline: none;
      font-family: var(--font-body);
      font-size: 0.9375rem;
      color: var(--text-main);
      width: 100%;
      background: transparent;
    }
    .search-divider {
      width: 1px;
      height: 32px;
      background: var(--border-light);
    }
    .search-btn {
      border-radius: var(--radius-lg);
      padding: 0.75rem 1.5rem;
    }
    .hero-stats {
      font-size: 0.875rem;
    }
    .stat-val {
      font-size: 1.5rem;
      font-weight: 800;
      color: var(--primary-dark);
      font-family: var(--font-heading);
    }
    .stat-lbl {
      color: var(--text-muted);
      font-size: 0.75rem;
      font-weight: 600;
    }
    .stat-sep {
      width: 1px;
      height: 36px;
      background: var(--border-strong);
    }
    .mandi-ticker-section {
      padding: 1.5rem 0;
      background: #f8fafc;
      border-top: 1px solid var(--border-light);
      border-bottom: 1px solid var(--border-light);
    }
    .ticker-scroll {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
      gap: 1rem;
    }
    .ticker-card {
      padding: 0.875rem 1rem;
      border-radius: var(--radius-md);
      background: #ffffff;
    }
    .category-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: 1.25rem;
    }
    .category-card {
      padding: 1.5rem 1rem;
      text-align: center;
      cursor: pointer;
      border-radius: var(--radius-lg);
      transition: all 0.2s ease;
    }
    .category-card:hover {
      transform: translateY(-4px);
      border-color: var(--primary);
    }
    .cat-icon-wrap {
      width: 56px;
      height: 56px;
      border-radius: var(--radius-lg);
      margin: 0 auto 1rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .cat-icon-wrap span { font-size: 28px; }
    .cat-name { font-size: 1rem; font-weight: 700; margin-bottom: 0.25rem; }
    .cat-count { font-size: 0.75rem; color: var(--text-muted); }
    .crops-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
      gap: 1.5rem;
    }
    .section-title {
      font-size: 2rem;
      font-weight: 800;
    }
    .section-sub {
      color: var(--text-muted);
      font-size: 0.9375rem;
    }
    .steps-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 1.5rem;
    }
    .step-card {
      position: relative;
      padding: 2rem 1.5rem;
      text-align: center;
      border-radius: var(--radius-lg);
    }
    .step-num {
      position: absolute;
      top: 1rem;
      left: 1rem;
      width: 28px;
      height: 28px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary);
      font-weight: 800;
      font-size: 0.8125rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .step-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary-dark);
      margin: 0 auto 1.25rem;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .step-icon span { font-size: 30px; }
    .step-card h3 { font-size: 1.125rem; margin-bottom: 0.5rem; }
    .step-card p { font-size: 0.875rem; color: var(--text-muted); line-height: 1.5; }
    .cta-section {
      padding: 3rem 0 5rem;
    }
    .cta-card {
      background: linear-gradient(135deg, #14532d 0%, #064e3b 100%);
      color: #ffffff;
      border-radius: var(--radius-xl);
      padding: 3.5rem 2.5rem;
      text-align: center;
      box-shadow: var(--shadow-xl);
    }
    .cta-content {
      max-width: 680px;
      margin: 0 auto;
    }
    .cta-content h2 { color: #ffffff; font-size: 2.25rem; margin-bottom: 1rem; }
    .cta-content p { color: #a7f3d0; font-size: 1.125rem; }
    .text-2xs { font-size: 0.625rem; }
    .py-12 { padding-top: 3rem; padding-bottom: 3rem; }
    .py-16 { padding-top: 4rem; padding-bottom: 4rem; }
    @media (max-width: 1024px) {
      .search-grid {
        grid-template-columns: 1fr;
      }
      .search-divider { display: none; }
      .hero-title { font-size: 2.25rem; }
    }
  `]
})
export class HomeComponent implements OnInit {
  private cropService = inject(CropService);
  private priceService = inject(PriceService);
  private router = inject(Router);

  searchCropName = '';
  selectedCategory = '';
  searchState = '';

  loadingCrops = true;
  featuredCrops: CropResponse[] = [];
  mandiRates: MandiPriceRecord[] = [];

  categories = [
    { id: 'VEGETABLES', name: 'Vegetables', desc: 'Tomato, Onion, Potato, Chilli', icon: 'nutrition', bg: '#ecfdf5', color: '#059669' },
    { id: 'FRUITS', name: 'Fruits', desc: 'Mango, Banana, Papaya, Citrus', icon: 'local_florist', bg: '#fef3c7', color: '#d97706' },
    { id: 'CEREALS', name: 'Grains & Cereals', desc: 'Wheat, Paddy, Maize, Bajra', icon: 'grain', bg: '#eff6ff', color: '#2563eb' },
    { id: 'PULSES', name: 'Pulses & Dal', desc: 'Gram, Tur, Moong, Urad', icon: 'spa', bg: '#faf5ff', color: '#7c3aed' },
    { id: 'SPICES', name: 'Spices', desc: 'Turmeric, Ginger, Cardamom', icon: 'local_fire_department', bg: '#fff1f2', color: '#e11d48' },
    { id: 'OILSEEDS', name: 'Oilseeds', desc: 'Mustard, Groundnut, Soya', icon: 'water_drop', bg: '#fefce8', color: '#ca8a04' }
  ];

  ngOnInit(): void {
    this.loadFeaturedCrops();
    this.loadMandiRates();
  }

  loadFeaturedCrops(): void {
    this.cropService.searchCrops({ size: 8 }).subscribe({
      next: (res) => {
        this.featuredCrops = res.content || [];
        this.loadingCrops = false;
      },
      error: () => {
        this.loadingCrops = false;
      }
    });
  }

  loadMandiRates(): void {
    this.priceService.getMandiRates().subscribe({
      next: (res) => {
        this.mandiRates = res || [];
      },
      error: () => {}
    });
  }

  onSearch(): void {
    this.router.navigate(['/crops'], {
      queryParams: {
        cropName: this.searchCropName || null,
        category: this.selectedCategory || null,
        state: this.searchState || null
      }
    });
  }

  selectCategory(categoryId: string): void {
    this.router.navigate(['/crops'], { queryParams: { category: categoryId } });
  }
}
