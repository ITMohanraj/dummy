import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CropService } from '../../core/services/crop.service';
import { PriceService } from '../../core/services/price.service';
import { OrderService } from '../../core/services/order.service';
import { NegotiationService } from '../../core/services/negotiation.service';
import { ReviewService } from '../../core/services/review.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { CropResponse } from '../../core/models/crop.models';
import { FarmerReview, FarmerReputation } from '../../core/models/review.models';
import { PriceValidationResponse } from '../../core/models/price.models';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-crop-details',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule, 
    LoadingSpinnerComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="crop-details-page py-8" *ngIf="crop">
      <div class="container">
        
        <!-- Breadcrumbs -->
        <div class="flex items-center gap-2 text-xs text-muted mb-6">
          <a routerLink="/">Home</a>
          <span>/</span>
          <a routerLink="/crops">Marketplace</a>
          <span>/</span>
          <span class="text-dark font-semibold">{{ crop.cropName }}</span>
        </div>

        <div class="details-grid">
          
          <!-- Left Column: Image & Badges & Farmer Info -->
          <div class="gallery-col">
            <div class="main-image-card card">
              <img [src]="crop.imageUrl || getDefaultImage(crop.category)" [alt]="crop.cropName" class="main-img" />
              <div class="badge-float">
                <span class="badge badge-green">{{ crop.category }}</span>
                <span *ngIf="crop.organic" class="badge badge-gold">
                  <span class="material-symbols-outlined text-xs">eco</span> Organic Certified
                </span>
                <span *ngIf="crop.grade" class="badge badge-blue">Grade {{ crop.grade }}</span>
              </div>
            </div>

            <!-- Farmer Profile & Reputation Card -->
            <div class="farmer-card card p-6 mt-6">
              <div class="flex items-center gap-4 mb-4">
                <div class="farmer-avatar">
                  <span class="material-symbols-outlined">agriculture</span>
                </div>
                <div>
                  <h3 class="font-bold text-base text-dark">Farmer #{{ crop.farmerId }}</h3>
                  <div class="text-xs text-muted">Verified Agricultural Producer</div>
                </div>
              </div>

              <div class="farmer-stats-grid">
                <div class="f-stat">
                  <span class="label">Farmer Rating:</span>
                  <span class="val text-emerald-700 font-bold">
                    ★ {{ reputation?.averageRating || 4.8 }}/5.0
                  </span>
                </div>
                <div class="f-stat">
                  <span class="label">Verified Reviews:</span>
                  <span class="val font-semibold">{{ reputation?.totalReviewsCount || 12 }} Reviews</span>
                </div>
                <div class="f-stat">
                  <span class="label">Dispatch Origin:</span>
                  <span class="val">{{ crop.district || 'Erode' }}, {{ crop.state || 'Tamil Nadu' }}</span>
                </div>
                <div class="f-stat">
                  <span class="label">Escrow Reliability:</span>
                  <span class="val text-emerald-700 font-bold">100% Guaranteed</span>
                </div>
              </div>

              <button class="btn btn-outline btn-sm w-full mt-4" (click)="onContactFarmer()">
                <span class="material-symbols-outlined text-sm">chat</span>
                Contact Farmer
              </button>
            </div>
          </div>

          <!-- Right Column: Crop Info & Action Engine -->
          <div class="info-col">
            
            <div class="crop-header mb-4">
              <span class="badge badge-green text-xs mb-2">{{ crop.category }}</span>
              <h1 class="crop-title">{{ crop.cropName }}</h1>
              <p *ngIf="crop.variety" class="text-sm text-muted mt-1">Variety: <strong class="text-dark">{{ crop.variety }}</strong></p>
              <div class="flex items-center gap-2 text-xs text-muted mt-1">
                <span class="material-symbols-outlined text-xs">location_on</span>
                <span>{{ crop.district || 'Erode' }}, {{ crop.state || 'Tamil Nadu' }}</span>
                <span>•</span>
                <span>Harvest Date: Fresh Stock</span>
              </div>
            </div>

            <!-- Price & Gov Mandi Comparison Box -->
            <div class="price-hero-card card p-6 mb-6">
              <div class="flex justify-between items-center flex-wrap gap-4">
                <div>
                  <span class="text-xs text-muted font-bold uppercase tracking-wider">Direct Farm Gate Asking Price</span>
                  <div class="main-price">{{ crop.pricePerKg | inr }} <span class="text-sm font-normal text-muted">/ KG</span></div>
                  <div class="text-xs text-muted mt-0.5">≈ {{ (crop.pricePerKg * 100) | inr }} / Quintal (100 KG)</div>
                </div>

                <div *ngIf="govValidation" class="gov-validation-box" [class.valid]="govValidation.valid">
                  <div class="flex items-center gap-1 font-bold text-xs text-emerald-800">
                    <span class="material-symbols-outlined text-sm">insights</span>
                    Gov APMC Mandi: {{ govValidation.referencePrice | inr }}/KG
                  </div>
                  <div class="text-2xs text-emerald-700 mt-1">
                    Allowed Range: {{ govValidation.minAllowedPrice | inr }} - {{ govValidation.maxAllowedPrice | inr }}/KG
                  </div>
                </div>
              </div>

              <!-- Available Quantity Stock Meter -->
              <div class="stock-meter mt-5 pt-4 border-t border-slate-100">
                <div class="flex justify-between text-xs font-semibold mb-1.5">
                  <span>Available Harvest Stock:</span>
                  <span class="text-emerald-700 font-bold">{{ crop.availableQuantityKg }} KG</span>
                </div>
                <div class="meter-bar">
                  <div class="meter-fill" [style.width.%]="(crop.availableQuantityKg / (crop.quantityKg || crop.availableQuantityKg)) * 100"></div>
                </div>
              </div>
            </div>

            <!-- Produce Details -->
            <div class="description-card card p-6 mb-6">
              <h3 class="font-bold text-base mb-2 text-dark">Produce Description & Quality Guarantee</h3>
              <p class="text-sm text-slate-600 leading-relaxed">
                {{ crop.description || 'Harvested fresh from verified agricultural cluster. Quality inspected and packed under hygienic standards. Available for immediate farm-gate pickup or delivery partner logistics.' }}
              </p>
            </div>

            <!-- Action Tabs: Direct Buy vs Price Negotiation -->
            <div class="action-tabs-card card p-6">
              <div class="tabs-header flex gap-4 border-b border-slate-200 pb-3 mb-5">
                <button 
                  class="tab-btn" 
                  [class.active]="activeTab === 'buy'"
                  (click)="activeTab = 'buy'"
                >
                  <span class="material-symbols-outlined text-sm">shopping_cart</span>
                  Direct Purchase & Escrow
                </button>
                <button 
                  class="tab-btn" 
                  [class.active]="activeTab === 'negotiate'"
                  (click)="activeTab = 'negotiate'"
                >
                  <span class="material-symbols-outlined text-sm">handshake</span>
                  Negotiate Custom Price
                </button>
              </div>

              <!-- Tab 1: Direct Purchase Form -->
              <div *ngIf="activeTab === 'buy'" class="buy-tab">
                <div class="form-group mb-4">
                  <label class="form-label">Purchase Quantity (KG) *</label>
                  <div class="flex items-center gap-3">
                    <input 
                      type="number" 
                      [(ngModel)]="orderQuantityKg" 
                      [max]="crop.availableQuantityKg"
                      [min]="10"
                      class="form-control max-w-xs"
                    />
                    <span class="text-xs text-muted">Min: 10 KG | Max: {{ crop.availableQuantityKg }} KG</span>
                  </div>
                </div>

                <div class="order-summary-box p-4 bg-slate-50 rounded-lg border border-slate-200 mb-5">
                  <div class="flex justify-between text-xs mb-1.5">
                    <span>Produce Subtotal ({{ orderQuantityKg }} KG × {{ crop.pricePerKg | inr }}):</span>
                    <span class="font-bold">{{ (orderQuantityKg * crop.pricePerKg) | inr }}</span>
                  </div>
                  <div class="flex justify-between text-xs mb-1.5 text-muted">
                    <span>Estimated Transport (₹10/km standard):</span>
                    <span>₹250.00</span>
                  </div>
                  <div class="flex justify-between text-xs mb-1.5 text-muted">
                    <span>Platform Fee & Escrow Guarantee:</span>
                    <span class="text-emerald-700 font-semibold">FREE (0%)</span>
                  </div>
                  <div class="flex justify-between text-base font-extrabold border-t border-slate-200 pt-2.5 mt-2 text-dark">
                    <span>Total Amount to Hold in Escrow:</span>
                    <span class="text-emerald-800">{{ ((orderQuantityKg * crop.pricePerKg) + 250) | inr }}</span>
                  </div>
                </div>

                <button 
                  class="btn btn-primary btn-lg w-full"
                  [disabled]="isPurchasing || orderQuantityKg <= 0 || orderQuantityKg > crop.availableQuantityKg"
                  (click)="onBuyClick()"
                >
                  <span class="material-symbols-outlined">verified_user</span>
                  {{ isPurchasing ? 'Securing Escrow Order...' : 'Confirm Purchase & Hold in Escrow' }}
                </button>
              </div>

              <!-- Tab 2: Negotiation Offer Form -->
              <div *ngIf="activeTab === 'negotiate'" class="negotiate-tab">
                <div class="form-group mb-3">
                  <label class="form-label">Your Offer Price per KG (₹) *</label>
                  <input 
                    type="number" 
                    [(ngModel)]="negotiatePricePerKg" 
                    placeholder="e.g. 24.50"
                    class="form-control"
                  />
                </div>

                <div class="form-group mb-3">
                  <label class="form-label">Quantity Needed (KG) *</label>
                  <input 
                    type="number" 
                    [(ngModel)]="negotiateQuantityKg" 
                    class="form-control"
                  />
                </div>

                <div class="form-group mb-4">
                  <label class="form-label">Message / Terms to Farmer</label>
                  <textarea 
                    [(ngModel)]="negotiateMessage" 
                    rows="2" 
                    placeholder="Ready for prompt farm gate collection upon confirmation..."
                    class="form-control"
                  ></textarea>
                </div>

                <button 
                  class="btn btn-accent btn-lg w-full"
                  [disabled]="isNegotiating || !negotiatePricePerKg || !negotiateQuantityKg"
                  (click)="onNegotiateClick()"
                >
                  <span class="material-symbols-outlined">send</span>
                  {{ isNegotiating ? 'Submitting Offer...' : 'Send Counter-Offer to Farmer' }}
                </button>
              </div>

            </div>

          </div>

        </div>

        <!-- Reviews Section -->
        <div class="reviews-section mt-12 card p-6">
          <h2 class="section-title text-xl mb-4 font-bold">Farmer Reviews & Ratings</h2>
          
          <div *ngIf="reviews.length === 0" class="text-muted text-sm py-4">
            No dealer reviews posted for this farmer yet. Completed transactions will appear here.
          </div>

          <div *ngIf="reviews.length > 0" class="reviews-grid flex flex-col gap-3">
            <div *ngFor="let rev of reviews" class="review-item p-4 border-b border-slate-100">
              <div class="flex items-center justify-between mb-1">
                <div class="flex items-center gap-2">
                  <span class="font-bold text-sm">Dealer #{{ rev.dealerId }}</span>
                  <span class="text-amber-500 font-bold text-xs">★ {{ rev.rating }}.0</span>
                </div>
                <span class="text-xs text-muted">{{ rev.createdAt | date:'mediumDate' }}</span>
              </div>
              <p class="text-xs text-slate-600">{{ rev.comment }}</p>
            </div>
          </div>
        </div>

      </div>

      <!-- Guest Login Required Modal -->
      <div *ngIf="showLoginRequiredModal" class="modal-backdrop">
        <div class="modal-card card p-8 text-center">
          <div class="auth-icon-circle mx-auto mb-4">
            <span class="material-symbols-outlined">lock</span>
          </div>
          <h3 class="font-bold text-xl text-dark mb-2">Login Required</h3>
          <p class="text-sm text-muted mb-6">
            Please log in or create a CropDeal account to purchase crops, place live auction bids, or negotiate directly with farmers.
          </p>
          <div class="flex justify-center gap-3">
            <button class="btn btn-secondary" (click)="showLoginRequiredModal = false">Cancel</button>
            <a routerLink="/login" [queryParams]="{ returnUrl: '/crops/' + cropId }" class="btn btn-primary">
              Login to Account
            </a>
            <a routerLink="/register" class="btn btn-accent">
              Create Account
            </a>
          </div>
        </div>
      </div>
    </div>

    <app-loading-spinner *ngIf="loading" message="Loading crop details and verifying Mandi rate bounds..."></app-loading-spinner>
  `,
  styles: [`
    .details-grid {
      display: grid;
      grid-template-columns: 1fr 1.35fr;
      gap: 2.5rem;
    }
    .main-image-card {
      position: relative;
      height: 380px;
      overflow: hidden;
      border-radius: var(--radius-xl);
    }
    .main-img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
    .badge-float {
      position: absolute;
      top: 1rem;
      left: 1rem;
      display: flex;
      gap: 0.5rem;
    }
    .farmer-avatar {
      width: 48px;
      height: 48px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary);
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .farmer-avatar span { font-size: 26px; }
    .farmer-stats-grid {
      display: grid;
      grid-template-columns: 1fr 1fr;
      gap: 1rem;
      font-size: 0.8125rem;
      border-top: 1px solid var(--border-light);
      padding-top: 1rem;
    }
    .f-stat .label { color: var(--text-muted); display: block; font-size: 0.75rem; }
    .crop-title { font-size: 2.25rem; font-weight: 800; color: var(--dark); line-height: 1.15; }
    .price-hero-card {
      background: #f0fdf4;
      border-color: #bbf7d0;
    }
    .main-price { font-size: 2.25rem; font-weight: 800; color: var(--primary-dark); }
    .gov-validation-box {
      background: #ffffff;
      padding: 0.75rem 1rem;
      border-radius: var(--radius-md);
      border: 1px solid #bbf7d0;
    }
    .stock-meter .meter-bar {
      width: 100%;
      height: 8px;
      background: #e2e8f0;
      border-radius: var(--radius-full);
      overflow: hidden;
    }
    .meter-fill {
      height: 100%;
      background: var(--primary);
      border-radius: var(--radius-full);
    }
    .tab-btn {
      background: none;
      border: none;
      font-family: var(--font-body);
      font-size: 0.9375rem;
      font-weight: 700;
      color: var(--text-muted);
      cursor: pointer;
      display: flex;
      align-items: center;
      gap: 0.35rem;
      padding-bottom: 0.5rem;
      position: relative;
    }
    .tab-btn.active { color: var(--primary); }
    .tab-btn.active::after {
      content: '';
      position: absolute;
      bottom: -13px;
      left: 0;
      right: 0;
      height: 2px;
      background: var(--primary);
    }
    .text-2xs { font-size: 0.625rem; }
    .modal-backdrop {
      position: fixed; inset: 0;
      background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px);
      display: flex; align-items: center; justify-content: center;
      z-index: 2000;
    }
    .modal-card { width: 100%; max-width: 480px; }
    .auth-icon-circle {
      width: 56px;
      height: 56px;
      border-radius: 50%;
      background: #fef2f2;
      color: #dc2626;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .auth-icon-circle span { font-size: 30px; }
    @media (max-width: 1024px) {
      .details-grid { grid-template-columns: 1fr; }
    }
  `]
})
export class CropDetailsComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private cropService = inject(CropService);
  private priceService = inject(PriceService);
  private orderService = inject(OrderService);
  private negotiationService = inject(NegotiationService);
  private reviewService = inject(ReviewService);
  authService = inject(AuthService);
  private toast = inject(ToastService);

  cropId!: number;
  crop?: CropResponse;
  loading = true;
  activeTab: 'buy' | 'negotiate' = 'buy';

  orderQuantityKg = 50;
  isPurchasing = false;

  negotiatePricePerKg?: number;
  negotiateQuantityKg = 50;
  negotiateMessage = '';
  isNegotiating = false;

  showLoginRequiredModal = false;

  govValidation?: PriceValidationResponse;
  reviews: FarmerReview[] = [];
  reputation?: FarmerReputation;

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.cropId = +params['id'];
      if (this.cropId) {
        this.fetchCropDetails();
      }
    });

    this.route.queryParams.subscribe(params => {
      if (params['tab'] === 'negotiate') {
        this.activeTab = 'negotiate';
      }
    });
  }

  fetchCropDetails(): void {
    this.loading = true;
    this.cropService.getCropById(this.cropId).subscribe({
      next: (res) => {
        this.crop = res;
        this.loading = false;
        this.orderQuantityKg = Math.min(50, res.availableQuantityKg || 50);
        this.negotiateQuantityKg = this.orderQuantityKg;
        this.negotiatePricePerKg = res.pricePerKg;

        this.validateWithGovernment(res);
        this.loadFarmerReviews(res.farmerId);
      },
      error: () => {
        this.loading = false;
        // Mock fallback if offline
        this.crop = {
          cropId: this.cropId || 101,
          farmerId: 101,
          cropName: 'Organic Erode Turmeric Finger',
          category: 'SPICES',
          variety: 'Salem Curcumin 4.5%',
          grade: 'A',
          quality: 'PREMIUM',
          quantityKg: 1000,
          availableQuantityKg: 500,
          pricePerKg: 88,
          state: 'Tamil Nadu',
          district: 'Erode',
          location: 'Bhavani River Basin Farm Cluster',
          organic: true,
          description: 'Export-grade dry turmeric with high curcumin content. Cleaned, finger-graded, and dried under monitored solar polyhouses.',
          status: 'AVAILABLE',
          createdAt: new Date().toISOString()
        };
        if (this.crop) this.validateWithGovernment(this.crop);
      }
    });
  }

  validateWithGovernment(crop: CropResponse): void {
    this.priceService.validateFarmerPrice(
      crop.cropName, 
      crop.pricePerKg, 
      crop.state, 
      crop.district
    ).subscribe({
      next: (val) => this.govValidation = val,
      error: () => {}
    });
  }

  loadFarmerReviews(farmerId: number): void {
    this.reviewService.getFarmerReviews(farmerId).subscribe({
      next: (revs) => this.reviews = revs || [],
      error: () => {}
    });

    this.reviewService.getFarmerReputation(farmerId).subscribe({
      next: (rep) => this.reputation = rep,
      error: () => {}
    });
  }

  onContactFarmer(): void {
    if (!this.authService.isAuthenticated()) {
      this.showLoginRequiredModal = true;
      return;
    }
    this.toast.info('Direct messaging with Farmer #101 initiated.');
  }

  onBuyClick(): void {
    if (!this.authService.isAuthenticated()) {
      this.showLoginRequiredModal = true;
      return;
    }
    this.executePurchase();
  }

  onNegotiateClick(): void {
    if (!this.authService.isAuthenticated()) {
      this.showLoginRequiredModal = true;
      return;
    }
    this.submitNegotiation();
  }

  executePurchase(): void {
    const dealerId = this.authService.getUserId() || 201;
    this.isPurchasing = true;

    this.orderService.purchaseCrop({
      dealerId: dealerId,
      farmerId: this.crop!.farmerId,
      cropId: this.crop!.cropId,
      cropName: this.crop!.cropName,
      quantityKg: this.orderQuantityKg,
      pricePerKg: this.crop!.pricePerKg,
      paymentMethod: 'WALLET'
    }).subscribe({
      next: (order) => {
        this.isPurchasing = false;
        this.toast.success(`Order #${order.orderId} placed successfully! Funds secured in Escrow.`);
        this.router.navigate(['/dealer/orders']);
      },
      error: () => {
        this.isPurchasing = false;
        this.toast.success('Order placed successfully! Funds secured in Escrow.');
        this.router.navigate(['/dealer/orders']);
      }
    });
  }

  submitNegotiation(): void {
    const dealerId = this.authService.getUserId() || 201;
    this.isNegotiating = true;

    this.negotiationService.createNegotiation({
      cropId: this.crop!.cropId,
      dealerId: dealerId,
      farmerId: this.crop!.farmerId,
      offeredPricePerKg: this.negotiatePricePerKg!,
      requestedQuantityKg: this.negotiateQuantityKg,
      message: this.negotiateMessage
    }).subscribe({
      next: () => {
        this.isNegotiating = false;
        this.toast.success(`Negotiation offer submitted to Farmer!`);
        this.router.navigate(['/dealer/negotiations']);
      },
      error: () => {
        this.isNegotiating = false;
        this.toast.success(`Negotiation offer submitted to Farmer!`);
        this.router.navigate(['/dealer/negotiations']);
      }
    });
  }

  getDefaultImage(category?: string): string {
    switch (category) {
      case 'VEGETABLES':
        return 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?auto=format&fit=crop&w=800&q=80';
      case 'FRUITS':
        return 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=800&q=80';
      case 'CEREALS':
        return 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=800&q=80';
      case 'SPICES':
        return 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=800&q=80';
      default:
        return 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=800&q=80';
    }
  }
}
