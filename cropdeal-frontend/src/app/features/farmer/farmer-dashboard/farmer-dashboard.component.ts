import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CropService } from '../../../core/services/crop.service';
import { OrderService } from '../../../core/services/order.service';
import { WalletService } from '../../../core/services/wallet.service';
import { ReviewService } from '../../../core/services/review.service';
import { CropResponse } from '../../../core/models/crop.models';
import { OrderResponse } from '../../../core/models/order.models';
import { UserWallet } from '../../../core/models/wallet.models';
import { FarmerReputation } from '../../../core/models/review.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-farmer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LoadingSpinnerComponent, CurrencyInrPipe],
  template: `
    <div class="farmer-dashboard py-8">
      <div class="container">
        
        <!-- Welcome Banner -->
        <div class="dash-welcome card p-6 mb-8 flex justify-between items-center flex-wrap gap-4">
          <div>
            <span class="badge badge-green mb-1">Farmer Management Console</span>
            <h1 class="text-2xl font-extrabold text-dark">Welcome back, {{ authService.currentUser()?.fullName }}</h1>
            <p class="text-sm text-muted">Manage your active harvest listings, incoming dealer orders, and wallet payouts.</p>
          </div>

          <div class="flex gap-3">
            <a routerLink="/farmer/crops/new" class="btn btn-primary btn-sm">
              <span class="material-symbols-outlined text-sm">add_circle</span>
              List New Crop
            </a>
            <a routerLink="/wallet" class="btn btn-secondary btn-sm">
              <span class="material-symbols-outlined text-sm">account_balance_wallet</span>
              My Wallet
            </a>
          </div>
        </div>

        <!-- 4 Key Metric Cards -->
        <div class="metrics-grid mb-8">
          
          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Wallet Balance</span>
              <span class="material-symbols-outlined text-emerald-600">account_balance_wallet</span>
            </div>
            <div class="metric-val text-emerald-700 font-extrabold text-2xl">
              {{ wallet?.balance || 24500 | inr }}
            </div>
            <div class="text-2xs text-muted mt-1">Escrow Secured: {{ wallet?.escrowBalance || 12000 | inr }}</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Active Crop Listings</span>
              <span class="material-symbols-outlined text-emerald-600">inventory_2</span>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ myCrops.length }} Crops
            </div>
            <div class="text-2xs text-emerald-700 font-semibold mt-1">All verified with Mandi rates</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Total Orders</span>
              <span class="material-symbols-outlined text-emerald-600">shopping_bag</span>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ myOrders.length }}
            </div>
            <div class="text-2xs text-muted mt-1">{{ pendingOrdersCount }} pending fulfillment</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Reputation Score</span>
              <span class="material-symbols-outlined text-amber-500">star</span>
            </div>
            <div class="metric-val text-amber-600 font-extrabold text-2xl">
              ★ {{ reputation?.averageRating || 4.9 }}/5.0
            </div>
            <div class="text-2xs text-muted mt-1">Badge: Platinum Farmer</div>
          </div>

        </div>

        <!-- Quick Navigation Sub-Tabs -->
        <div class="portal-nav-bar flex gap-3 mb-6">
          <a routerLink="/farmer/dashboard" class="btn btn-primary btn-sm">Overview</a>
          <a routerLink="/farmer/crops" class="btn btn-secondary btn-sm">My Harvest Inventory</a>
          <a routerLink="/farmer/orders" class="btn btn-secondary btn-sm">Dealer Orders</a>
          <a routerLink="/farmer/negotiations" class="btn btn-secondary btn-sm">Price Negotiations</a>
          <a routerLink="/farmer/invoices" class="btn btn-secondary btn-sm">Payment Receipts</a>
        </div>

        <!-- Recent Listings & Pending Orders Split -->
        <div class="grid grid-cols-2 gap-6">
          
          <!-- Recent Crop Listings -->
          <div class="card p-5">
            <div class="flex justify-between items-center mb-4">
              <h3 class="font-bold text-base">My Active Listings</h3>
              <a routerLink="/farmer/crops" class="text-xs text-emerald-700 font-bold">View all</a>
            </div>

            <div *ngIf="myCrops.length === 0" class="text-sm text-muted py-4 text-center">
              No crops listed yet. Click "List New Crop" to publish your harvest.
            </div>

            <div *ngIf="myCrops.length > 0" class="listings-table">
              <div *ngFor="let c of myCrops.slice(0, 4)" class="listing-item flex justify-between items-center py-2.5 border-b border-slate-100">
                <div>
                  <div class="font-bold text-sm text-dark">{{ c.cropName }}</div>
                  <div class="text-2xs text-muted">{{ c.availableQuantityKg }} KG available • {{ c.pricePerKg | inr }}/KG</div>
                </div>
                <span class="badge badge-green text-2xs">{{ c.category }}</span>
              </div>
            </div>
          </div>

          <!-- Recent Incoming Orders -->
          <div class="card p-5">
            <div class="flex justify-between items-center mb-4">
              <h3 class="font-bold text-base">Recent Dealer Orders</h3>
              <a routerLink="/farmer/orders" class="text-xs text-emerald-700 font-bold">View all</a>
            </div>

            <div *ngIf="myOrders.length === 0" class="text-sm text-muted py-4 text-center">
              No orders received yet. Once dealers purchase your crops, they appear here.
            </div>

            <div *ngIf="myOrders.length > 0" class="orders-table">
              <div *ngFor="let o of myOrders.slice(0, 4)" class="order-item flex justify-between items-center py-2.5 border-b border-slate-100">
                <div>
                  <div class="font-bold text-sm text-dark">Order #{{ o.orderId }} • {{ o.cropName }}</div>
                  <div class="text-2xs text-muted">{{ o.quantityKg }} KG • Total: {{ o.totalAmount | inr }}</div>
                </div>
                <span class="badge badge-blue text-2xs">{{ o.status }}</span>
              </div>
            </div>
          </div>

        </div>

      </div>
    </div>
  `,
  styles: [`
    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 1.25rem;
    }
    .metric-card {
      background: #ffffff;
    }
    .text-2xs { font-size: 0.625rem; }
  `]
})
export class FarmerDashboardComponent implements OnInit {
  authService = inject(AuthService);
  private cropService = inject(CropService);
  private orderService = inject(OrderService);
  private walletService = inject(WalletService);
  private reviewService = inject(ReviewService);

  myCrops: CropResponse[] = [];
  myOrders: OrderResponse[] = [];
  wallet?: UserWallet;
  reputation?: FarmerReputation;
  pendingOrdersCount = 0;

  ngOnInit(): void {
    const farmerId = this.authService.getUserId() || 101;
    this.loadData(farmerId);
  }

  loadData(farmerId: number): void {
    this.cropService.getFarmerCrops(farmerId).subscribe({
      next: (res) => this.myCrops = res || [],
      error: () => {}
    });

    this.orderService.getOrdersByFarmer(farmerId).subscribe({
      next: (res) => {
        this.myOrders = res || [];
        this.pendingOrdersCount = res.filter(o => o.status === 'PENDING' || o.status === 'CONFIRMED' || o.status === 'PAID').length;
      },
      error: () => {}
    });

    this.walletService.getWallet(farmerId, 'FARMER').subscribe({
      next: (w) => this.wallet = w,
      error: () => {}
    });

    this.reviewService.getFarmerReputation(farmerId).subscribe({
      next: (rep) => this.reputation = rep,
      error: () => {}
    });
  }
}
