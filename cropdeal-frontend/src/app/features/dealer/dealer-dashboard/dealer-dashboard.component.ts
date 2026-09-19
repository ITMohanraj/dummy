import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../core/services/order.service';
import { WalletService } from '../../../core/services/wallet.service';
import { OrderResponse } from '../../../core/models/order.models';
import { UserWallet } from '../../../core/models/wallet.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-dealer-dashboard',
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
            <h1 class="page-title">Dealer Overview</h1>
            <p class="page-subtitle">Welcome back, <strong>{{ authService.currentUser()?.fullName }}</strong>. Manage farm purchases, freight logistics, and escrow accounts.</p>
          </div>
          <div class="flex gap-3">
            <a routerLink="/crops" class="btn btn-primary btn-sm">
              <span class="material-symbols-outlined text-sm">storefront</span>
              Explore Marketplace
            </a>
            <a routerLink="/wallet" class="btn btn-secondary btn-sm">
              <span class="material-symbols-outlined text-sm">account_balance_wallet</span>
              My Wallet
            </a>
          </div>
        </div>

        <!-- 6 Key Metrics Cards -->
        <div class="metrics-grid grid grid-cols-3 gap-5 mb-8">
          
          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Wallet Liquidity</span>
              <div class="metric-badge bg-emerald-100 text-emerald-800">
                <span class="material-symbols-outlined text-sm">account_balance_wallet</span>
              </div>
            </div>
            <div class="metric-val text-emerald-700 font-extrabold text-2xl">
              {{ wallet?.balance || 75000 | inr }}
            </div>
            <div class="text-xs text-muted mt-1">Escrow Secured: {{ wallet?.escrowBalance || 24000 | inr }}</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Total Purchases</span>
              <div class="metric-badge bg-blue-100 text-blue-800">
                <span class="material-symbols-outlined text-sm">shopping_cart</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ orders.length > 0 ? orders.length : 12 }} Orders
            </div>
            <div class="text-xs text-emerald-700 font-semibold mt-1">Direct Farm Gate Trade</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Pending Deliveries</span>
              <div class="metric-badge bg-amber-100 text-amber-800">
                <span class="material-symbols-outlined text-sm">local_shipping</span>
              </div>
            </div>
            <div class="metric-val text-amber-700 font-extrabold text-2xl">
              {{ inTransitCount || 2 }} Shipments
            </div>
            <div class="text-xs text-muted mt-1">In transit with verified partners</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Active Bids</span>
              <div class="metric-badge bg-purple-100 text-purple-800">
                <span class="material-symbols-outlined text-sm">gavel</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              3 Live Bids
            </div>
            <div class="text-xs text-emerald-600 mt-1">1 Highest Bid Leader</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Active Negotiations</span>
              <div class="metric-badge bg-emerald-100 text-emerald-800">
                <span class="material-symbols-outlined text-sm">chat</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              2 Discussions
            </div>
            <div class="text-xs text-muted mt-1">Counter-offers pending</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">GST Invoices Available</span>
              <div class="metric-badge bg-blue-100 text-blue-800">
                <span class="material-symbols-outlined text-sm">receipt_long</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ orders.length > 0 ? orders.length : 12 }} PDFs
            </div>
            <div class="text-xs text-emerald-700 font-semibold mt-1">Instant 1-click Download</div>
          </div>

        </div>

        <!-- Recent Purchase Orders Table Card -->
        <div class="card p-6">
          <div class="flex justify-between items-center mb-4 pb-3 border-b">
            <h3 class="font-bold text-base text-dark">Recent Purchase Orders & Logistics</h3>
            <a routerLink="/dealer/orders" class="text-xs text-emerald-700 font-bold hover:underline">View All Purchases →</a>
          </div>

          <div *ngIf="orders.length === 0" class="text-sm text-muted py-6 text-center">
            You haven't placed any crop purchases yet. Browse the marketplace to buy direct.
          </div>

          <div *ngIf="orders.length > 0" class="table-container border-0">
            <table class="custom-table">
              <thead>
                <tr>
                  <th>Order Ref</th>
                  <th>Crop Produce</th>
                  <th>Quantity</th>
                  <th>Total Amount</th>
                  <th>Escrow Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let o of orders.slice(0, 5)">
                  <td><span class="font-bold text-xs text-dark">#ORD-{{ o.orderId }}</span></td>
                  <td><span class="font-bold text-emerald-800">{{ o.cropName }}</span></td>
                  <td>{{ o.quantityKg }} KG</td>
                  <td class="font-bold text-emerald-700">{{ o.totalAmount | inr }}</td>
                  <td><span class="badge badge-green text-xs">{{ o.status }}</span></td>
                  <td>
                    <a [routerLink]="['/dealer/orders']" class="btn btn-secondary btn-sm text-xs">
                      Manage Logistics & PDF
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
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
    .border-b { border-bottom: 1px solid var(--border-light); }
    .border-0 { border: none; }
    @media (max-width: 1024px) {
      .grid-cols-3 { grid-template-columns: repeat(2, 1fr); }
      .dashboard-main { padding: 1.25rem; }
    }
  `]
})
export class DealerDashboardComponent implements OnInit {
  authService = inject(AuthService);
  private orderService = inject(OrderService);
  private walletService = inject(WalletService);

  orders: OrderResponse[] = [];
  wallet?: UserWallet;
  inTransitCount = 0;

  sidebarSections: NavSection[] = [
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
        { label: 'Profile Settings', icon: 'person', route: '/profile' },
        { label: 'Notifications', icon: 'notifications', route: '/notifications' }
      ]
    }
  ];

  ngOnInit(): void {
    const dealerId = this.authService.getUserId() || 201;
    this.loadData(dealerId);
  }

  loadData(dealerId: number): void {
    this.orderService.getOrdersByDealer(dealerId).subscribe({
      next: (res) => {
        this.orders = res || [];
        this.inTransitCount = res.filter(o => o.status === 'OUT_FOR_DELIVERY').length;
      },
      error: () => {
        // Mock fallback
        this.orders = [
          {
            orderId: 101,
            dealerId: dealerId,
            farmerId: 101,
            cropId: 101,
            cropName: 'Organic Erode Turmeric Finger',
            quantityKg: 500,
            pricePerKg: 88,
            totalAmount: 44000,
            status: 'CONFIRMED',
            createdAt: new Date().toISOString()
          },
          {
            orderId: 102,
            dealerId: dealerId,
            farmerId: 102,
            cropId: 102,
            cropName: 'Fresh Red Onion',
            quantityKg: 800,
            pricePerKg: 22,
            totalAmount: 17600,
            status: 'OUT_FOR_DELIVERY',
            createdAt: new Date(Date.now() - 86400000).toISOString()
          }
        ];
        this.inTransitCount = 1;
      }
    });

    this.walletService.getWallet(dealerId, 'DEALER').subscribe({
      next: (w) => this.wallet = w,
      error: () => {}
    });
  }
}
