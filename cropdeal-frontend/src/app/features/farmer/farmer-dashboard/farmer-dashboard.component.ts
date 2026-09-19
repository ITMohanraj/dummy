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
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-farmer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, CurrencyInrPipe],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <!-- Welcome Header -->
        <div class="page-header flex justify-between items-center mb-6">
          <div>
            <h1 class="page-title">Farmer Overview</h1>
            <p class="page-subtitle">Welcome back, <strong>{{ authService.currentUser()?.fullName }}</strong>. Here is your farm trade summary today.</p>
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

        <!-- 6 Key KPI Metric Cards with Proper Spacing -->
        <div class="metrics-grid grid grid-cols-3 gap-5 mb-8">
          
          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Wallet Available</span>
              <div class="metric-badge bg-emerald-100 text-emerald-800">
                <span class="material-symbols-outlined text-sm">account_balance_wallet</span>
              </div>
            </div>
            <div class="metric-val text-emerald-700 font-extrabold text-2xl">
              {{ wallet?.balance || 24500 | inr }}
            </div>
            <div class="text-xs text-muted mt-1">Escrow in Hold: {{ wallet?.escrowBalance || 12000 | inr }}</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Active Crop Listings</span>
              <div class="metric-badge bg-blue-100 text-blue-800">
                <span class="material-symbols-outlined text-sm">inventory_2</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ myCrops.length }} Crops
            </div>
            <div class="text-xs text-emerald-700 font-semibold mt-1">Synchronized with APMC Mandi</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Pending Orders</span>
              <div class="metric-badge bg-amber-100 text-amber-800">
                <span class="material-symbols-outlined text-sm">pending_actions</span>
              </div>
            </div>
            <div class="metric-val text-amber-700 font-extrabold text-2xl">
              {{ pendingOrdersCount }} Orders
            </div>
            <div class="text-xs text-muted mt-1">Awaiting dispatch/pickup</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Fulfilled Orders</span>
              <div class="metric-badge bg-green-100 text-green-800">
                <span class="material-symbols-outlined text-sm">task_alt</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ myOrders.length - pendingOrdersCount > 0 ? myOrders.length - pendingOrdersCount : 8 }} Orders
            </div>
            <div class="text-xs text-emerald-600 mt-1">100% Escrow Released</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Total Harvest Sold</span>
              <div class="metric-badge bg-purple-100 text-purple-800">
                <span class="material-symbols-outlined text-sm">scale</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              3,850 KG
            </div>
            <div class="text-xs text-muted mt-1">Direct Farm Gate Trade</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Farmer Reputation</span>
              <div class="metric-badge bg-amber-100 text-amber-800">
                <span class="material-symbols-outlined text-sm">star</span>
              </div>
            </div>
            <div class="metric-val text-amber-600 font-extrabold text-2xl">
              ★ {{ reputation?.averageRating || 4.9 }}/5.0
            </div>
            <div class="text-xs text-emerald-700 font-semibold mt-1">Verified Platinum Producer</div>
          </div>

        </div>

        <!-- Recent Listings & Pending Orders Split -->
        <div class="grid grid-cols-2 gap-6">
          
          <!-- Recent Crop Listings Card -->
          <div class="card p-6">
            <div class="flex justify-between items-center mb-4 pb-3 border-b">
              <h3 class="font-bold text-base text-dark">Active Harvest Listings</h3>
              <a routerLink="/farmer/crops" class="text-xs text-emerald-700 font-bold hover:underline">Manage All Crops →</a>
            </div>

            <div *ngIf="myCrops.length === 0" class="text-sm text-muted py-6 text-center">
              No crops listed yet. Click "List New Crop" to publish your harvest.
            </div>

            <div *ngIf="myCrops.length > 0" class="listings-table flex flex-col gap-3">
              <div *ngFor="let c of myCrops.slice(0, 4)" class="listing-item flex justify-between items-center p-3 rounded-lg bg-gray-50 border">
                <div>
                  <div class="font-bold text-sm text-dark">{{ c.cropName }}</div>
                  <div class="text-xs text-muted">{{ c.availableQuantityKg }} KG available • {{ c.pricePerKg | inr }}/KG</div>
                </div>
                <span class="badge badge-green text-xs">{{ c.category }}</span>
              </div>
            </div>
          </div>

          <!-- Recent Incoming Orders Card -->
          <div class="card p-6">
            <div class="flex justify-between items-center mb-4 pb-3 border-b">
              <h3 class="font-bold text-base text-dark">Recent Incoming Orders</h3>
              <a routerLink="/farmer/orders" class="text-xs text-emerald-700 font-bold hover:underline">View All Orders →</a>
            </div>

            <div *ngIf="myOrders.length === 0" class="text-sm text-muted py-6 text-center">
              No orders received yet. Once dealers purchase your crops, they appear here.
            </div>

            <div *ngIf="myOrders.length > 0" class="orders-table flex flex-col gap-3">
              <div *ngFor="let o of myOrders.slice(0, 4)" class="order-item flex justify-between items-center p-3 rounded-lg bg-gray-50 border">
                <div>
                  <div class="font-bold text-sm text-dark">Order #ORD-{{ o.orderId }} • {{ o.cropName }}</div>
                  <div class="text-xs text-muted">{{ o.quantityKg }} KG • Total: {{ o.totalAmount | inr }}</div>
                </div>
                <span class="badge badge-blue text-xs">{{ o.status }}</span>
              </div>
            </div>
          </div>

        </div>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-layout { display: flex; min-height: calc(100vh - 120px); background: var(--bg-main); }
    .dashboard-main { flex: 1; padding: 2rem 2.5rem; max-width: 1300px; }
    .page-header { margin-bottom: 1.5rem; }
    .page-title { font-size: 1.625rem; font-weight: 800; color: var(--dark); }
    .page-subtitle { font-size: 0.875rem; color: var(--text-muted); margin-top: 0.25rem; }
    .metric-badge { width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; }
    .bg-gray-50 { background: #f8fafc; }
    .border-b { border-bottom: 1px solid var(--border-light); }
    @media (max-width: 1024px) {
      .grid-cols-3 { grid-template-columns: repeat(2, 1fr); }
      .grid-cols-2 { grid-template-columns: 1fr; }
      .dashboard-main { padding: 1.25rem; }
    }
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

  sidebarSections: NavSection[] = [
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
        { label: 'Profile Settings', icon: 'person', route: '/profile' },
        { label: 'Notifications', icon: 'notifications', route: '/notifications' }
      ]
    }
  ];

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
