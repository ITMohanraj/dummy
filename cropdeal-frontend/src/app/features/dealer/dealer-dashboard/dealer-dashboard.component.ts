import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { OrderService } from '../../../core/services/order.service';
import { WalletService } from '../../../core/services/wallet.service';
import { OrderResponse } from '../../../core/models/order.models';
import { UserWallet } from '../../../core/models/wallet.models';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-dealer-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyInrPipe],
  template: `
    <div class="dealer-dashboard py-8">
      <div class="container">
        
        <!-- Welcome Banner -->
        <div class="dash-welcome card p-6 mb-8 flex justify-between items-center flex-wrap gap-4">
          <div>
            <span class="badge badge-gold mb-1">Dealer B2B Trade Floor</span>
            <h1 class="text-2xl font-extrabold text-dark">Welcome, {{ authService.currentUser()?.fullName }}</h1>
            <p class="text-sm text-muted">Procure bulk harvest from farmers, manage logistics, and download tax invoices.</p>
          </div>

          <div class="flex gap-3">
            <a routerLink="/crops" class="btn btn-primary btn-sm">
              <span class="material-symbols-outlined text-sm">storefront</span>
              Explore Crops
            </a>
            <a routerLink="/wallet" class="btn btn-secondary btn-sm">
              <span class="material-symbols-outlined text-sm">account_balance_wallet</span>
              Wallet Balance
            </a>
          </div>
        </div>

        <!-- 4 Key Metrics -->
        <div class="metrics-grid mb-8">
          
          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Wallet Liquidity</span>
              <span class="material-symbols-outlined text-emerald-600">account_balance_wallet</span>
            </div>
            <div class="metric-val text-emerald-700 font-extrabold text-2xl">
              {{ wallet?.balance || 50000 | inr }}
            </div>
            <div class="text-2xs text-muted mt-1">Escrow Locked: {{ wallet?.escrowBalance || 18500 | inr }}</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Active Purchases</span>
              <span class="material-symbols-outlined text-emerald-600">shopping_cart</span>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ orders.length }} Orders
            </div>
            <div class="text-2xs text-emerald-700 font-semibold mt-1">Direct farm procurement</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Shipments in Transit</span>
              <span class="material-symbols-outlined text-blue-600">local_shipping</span>
            </div>
            <div class="metric-val text-blue-700 font-extrabold text-2xl">
              {{ inTransitCount }} Shipments
            </div>
            <div class="text-2xs text-muted mt-1">₹10/km delivery fleet</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Tax Invoices</span>
              <span class="material-symbols-outlined text-amber-500">receipt</span>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ orders.length }} PDFs
            </div>
            <div class="text-2xs text-muted mt-1">GST & APMC Compliant</div>
          </div>

        </div>

        <!-- Sub Tabs -->
        <div class="portal-nav-bar flex gap-3 mb-6">
          <a routerLink="/dealer/dashboard" class="btn btn-primary btn-sm">Overview</a>
          <a routerLink="/dealer/orders" class="btn btn-secondary btn-sm">My Purchases & Tracking</a>
          <a routerLink="/crops" class="btn btn-secondary btn-sm">Browse Marketplace</a>
          <a routerLink="/market-prices" class="btn btn-secondary btn-sm">Live Mandi Trends</a>
          <a routerLink="/auctions" class="btn btn-secondary btn-sm">Live Auctions Floor</a>
        </div>

        <!-- Recent Purchases Table -->
        <div class="card p-6">
          <div class="flex justify-between items-center mb-4">
            <h3 class="font-bold text-base">Recent Purchase Orders</h3>
            <a routerLink="/dealer/orders" class="text-xs text-emerald-700 font-bold">View all orders</a>
          </div>

          <div *ngIf="orders.length === 0" class="text-sm text-muted py-6 text-center">
            You haven't placed any crop purchases yet. Browse the marketplace to buy direct.
          </div>

          <div *ngIf="orders.length > 0" class="table-container">
            <table class="custom-table">
              <thead>
                <tr>
                  <th>Order ID</th>
                  <th>Produce</th>
                  <th>Farmer</th>
                  <th>Quantity</th>
                  <th>Total Amount</th>
                  <th>Escrow Status</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let o of orders.slice(0, 5)">
                  <td>#{{ o.orderId }}</td>
                  <td><span class="font-bold text-emerald-800">{{ o.cropName }}</span></td>
                  <td>Farmer #{{ o.farmerId }}</td>
                  <td>{{ o.quantityKg }} KG</td>
                  <td><strong>{{ o.totalAmount | inr }}</strong></td>
                  <td><span class="badge badge-green text-2xs">{{ o.status }}</span></td>
                  <td>
                    <a [routerLink]="['/dealer/orders']" class="btn btn-secondary btn-sm text-xs">
                      Manage Logistics
                    </a>
                  </td>
                </tr>
              </tbody>
            </table>
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
    .text-2xs { font-size: 0.625rem; }
  `]
})
export class DealerDashboardComponent implements OnInit {
  authService = inject(AuthService);
  private orderService = inject(OrderService);
  private walletService = inject(WalletService);

  orders: OrderResponse[] = [];
  wallet?: UserWallet;
  inTransitCount = 0;

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
      error: () => {}
    });

    this.walletService.getWallet(dealerId, 'DEALER').subscribe({
      next: (w) => this.wallet = w,
      error: () => {}
    });
  }
}
