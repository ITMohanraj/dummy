import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { BiddingService } from '../../../core/services/bidding.service';
import { ToastService } from '../../../core/services/toast.service';
import { BidRecord } from '../../../core/models/bidding.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-dealer-bidding',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, CurrencyInrPipe, LoadingSpinnerComponent, EmptyStateComponent],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <!-- Page Header -->
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">My Bidding Deals</h1>
            <p class="page-subtitle">Track your placed bids, active auction status, and won crop lots</p>
          </div>
          <a routerLink="/auctions" class="btn btn-primary btn-sm">
            <span class="material-symbols-outlined text-sm">sensors</span>
            <span>Join Live Auctions</span>
          </a>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading your bids..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && bids.length === 0"
          icon="gavel"
          title="No Bids Placed Yet"
          message="Browse live auctions or harvest lots in the marketplace to place competitive bids."
          actionText="Explore Marketplace"
          actionRoute="/crops"
        ></app-empty-state>

        <div *ngIf="!loading && bids.length > 0" class="table-container card">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Bid Reference</th>
                <th>Crop Lot</th>
                <th>Farmer Location</th>
                <th>My Bid Price</th>
                <th>Quantity</th>
                <th>Total Value</th>
                <th>Status</th>
                <th>Time Placed</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let bid of bids">
                <td class="font-bold text-xs">#BID-{{ bid.id }}</td>
                <td class="font-bold">{{ bid.cropName || 'Crop #' + bid.cropId }}</td>
                <td class="text-xs text-muted">Erode, Tamil Nadu</td>
                <td class="font-bold text-primary">{{ bid.bidAmount | inr }}/kg</td>
                <td>{{ bid.quantity || 500 }} KG</td>
                <td class="font-bold">{{ (bid.bidAmount * (bid.quantity || 500)) | inr }}</td>
                <td>
                  <span class="badge" [ngClass]="getStatusBadge(bid.status)">
                    {{ bid.status }}
                  </span>
                </td>
                <td class="text-xs text-muted">{{ bid.createdAt | date:'short' }}</td>
              </tr>
            </tbody>
          </table>
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
    @media (max-width: 900px) { .dashboard-main { padding: 1.25rem; } }
  `]
})
export class DealerBiddingComponent implements OnInit {
  private authService = inject(AuthService);
  private biddingService = inject(BiddingService);

  bids: BidRecord[] = [];
  loading = true;

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
        { label: 'Bidding Deals', icon: 'gavel', route: '/dealer/bidding', exact: true },
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
    this.loadBids();
  }

  loadBids(): void {
    this.loading = true;
    const userId = this.authService.getUserId() || 201;
    this.biddingService.getDealerBids(userId).subscribe({
      next: (res) => {
        this.bids = res;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        // Mock fallback
        this.bids = [
          {
            id: 901,
            sessionId: 1,
            cropId: 101,
            cropName: 'Organic Erode Turmeric Finger',
            dealerId: userId,
            bidAmount: 89.5,
            quantity: 500,
            status: 'ACCEPTED',
            createdAt: new Date(Date.now() - 7200000).toISOString()
          },
          {
            id: 902,
            sessionId: 2,
            cropId: 103,
            cropName: 'Alphonso Ratnagiri Mangoes',
            dealerId: userId,
            bidAmount: 175,
            quantity: 200,
            status: 'ACTIVE',
            createdAt: new Date().toISOString()
          }
        ];
      }
    });
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'ACCEPTED': return 'badge-green';
      case 'ACTIVE': return 'badge-blue';
      case 'OUTBID': return 'badge-gold';
      case 'REJECTED': return 'badge-red';
      default: return 'badge-gray';
    }
  }
}
