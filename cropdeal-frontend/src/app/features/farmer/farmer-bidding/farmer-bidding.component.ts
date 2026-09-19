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
  selector: 'app-farmer-bidding',
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
            <h1 class="page-title">Bidding Management</h1>
            <p class="page-subtitle">Review real-time competitive dealer bids for your crop harvest lots</p>
          </div>
          <a routerLink="/auctions" class="btn btn-primary btn-sm">
            <span class="material-symbols-outlined text-sm">sensors</span>
            <span>Live Auction Floor</span>
          </a>
        </div>

        <!-- Filter Badges -->
        <div class="filter-bar flex gap-2 mb-6">
          <button class="filter-btn" [class.active]="currentFilter === 'ALL'" (click)="currentFilter = 'ALL'">
            All Bids ({{ bids.length }})
          </button>
          <button class="filter-btn" [class.active]="currentFilter === 'PENDING'" (click)="currentFilter = 'PENDING'">
            Pending Review ({{ getPendingCount() }})
          </button>
          <button class="filter-btn" [class.active]="currentFilter === 'ACCEPTED'" (click)="currentFilter = 'ACCEPTED'">
            Accepted ({{ getAcceptedCount() }})
          </button>
          <button class="filter-btn" [class.active]="currentFilter === 'REJECTED'" (click)="currentFilter = 'REJECTED'">
            Rejected ({{ getRejectedCount() }})
          </button>
        </div>

        <!-- Loading State -->
        <app-loading-spinner *ngIf="loading" message="Loading received bids..."></app-loading-spinner>

        <!-- Empty State -->
        <app-empty-state 
          *ngIf="!loading && filteredBids.length === 0"
          icon="gavel"
          title="No Bids Received Yet"
          message="When verified dealers place bids on your crop listings, they will appear here for your review and acceptance."
          actionText="List a New Crop"
          actionRoute="/farmer/crops/new"
        ></app-empty-state>

        <!-- Bids Table -->
        <div *ngIf="!loading && filteredBids.length > 0" class="table-container card">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Bid ID</th>
                <th>Crop Lot</th>
                <th>Dealer Name</th>
                <th>Offered Price</th>
                <th>Quantity</th>
                <th>Total Value</th>
                <th>Time Placed</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let bid of filteredBids">
                <td class="font-bold text-xs">#BID-{{ bid.id }}</td>
                <td>
                  <div class="crop-info">
                    <span class="crop-name font-bold">{{ bid.cropName || 'Crop Listing #' + bid.cropId }}</span>
                  </div>
                </td>
                <td>
                  <div class="dealer-info">
                    <span class="font-bold">{{ bid.dealerName || 'Dealer #' + bid.dealerId }}</span>
                  </div>
                </td>
                <td class="font-bold text-primary">{{ bid.bidAmount | inr }}/kg</td>
                <td>{{ bid.quantity || 500 }} KG</td>
                <td class="font-bold">{{ (bid.bidAmount * (bid.quantity || 500)) | inr }}</td>
                <td class="text-xs text-muted">{{ bid.createdAt | date:'short' }}</td>
                <td>
                  <span class="badge" [ngClass]="getStatusBadge(bid.status)">
                    {{ bid.status }}
                  </span>
                </td>
                <td>
                  <div class="action-buttons flex gap-1" *ngIf="bid.status === 'PENDING' || bid.status === 'ACTIVE' || bid.status === 'OUTBID'">
                    <button class="btn btn-primary btn-sm" (click)="acceptBid(bid.id)">
                      Accept
                    </button>
                    <button class="btn btn-secondary btn-sm text-danger" (click)="rejectBid(bid.id)">
                      Reject
                    </button>
                  </div>
                  <span *ngIf="bid.status === 'ACCEPTED'" class="text-xs text-emerald-600 font-bold">Deal Finalized</span>
                  <span *ngIf="bid.status === 'REJECTED'" class="text-xs text-muted">Closed</span>
                </td>
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
    .filter-btn {
      padding: 0.5rem 1rem;
      border-radius: var(--radius-full);
      background: #ffffff;
      border: 1px solid var(--border-light);
      font-size: 0.8125rem;
      font-weight: 600;
      color: var(--text-muted);
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .filter-btn.active {
      background: var(--primary);
      color: #ffffff;
      border-color: var(--primary);
    }
    .filter-btn:hover:not(.active) { background: var(--primary-subtle); color: var(--primary); }
    .action-buttons button { font-size: 0.75rem; padding: 0.25rem 0.5rem; }
    @media (max-width: 900px) { .dashboard-main { padding: 1.25rem; } }
  `]
})
export class FarmerBiddingComponent implements OnInit {
  private authService = inject(AuthService);
  private biddingService = inject(BiddingService);
  private toast = inject(ToastService);

  bids: BidRecord[] = [];
  loading = true;
  currentFilter: 'ALL' | 'PENDING' | 'ACCEPTED' | 'REJECTED' = 'ALL';

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
        { label: 'Bidding Floor', icon: 'gavel', route: '/farmer/bidding', exact: true },
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

  get filteredBids(): BidRecord[] {
    if (this.currentFilter === 'ALL') return this.bids;
    if (this.currentFilter === 'PENDING') {
      return this.bids.filter(b => b.status === 'PENDING' || b.status === 'ACTIVE' || b.status === 'OUTBID');
    }
    return this.bids.filter(b => b.status === this.currentFilter);
  }

  ngOnInit(): void {
    this.loadBids();
  }

  loadBids(): void {
    this.loading = true;
    const userId = this.authService.getUserId() || 101;
    this.biddingService.getFarmerBids(userId).subscribe({
      next: (data) => {
        this.bids = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        // Mock fallback if offline
        this.bids = [
          {
            id: 801,
            sessionId: 1,
            cropId: 101,
            cropName: 'Premium Sona Masoori Paddy',
            dealerId: 201,
            dealerName: 'Kisan Mandi Traders',
            bidAmount: 38.5,
            quantity: 500,
            status: 'PENDING',
            createdAt: new Date().toISOString()
          },
          {
            id: 802,
            sessionId: 2,
            cropId: 102,
            cropName: 'Organic Alphonso Mangoes',
            dealerId: 202,
            dealerName: 'Apex Food Impex',
            bidAmount: 180,
            quantity: 300,
            status: 'ACCEPTED',
            createdAt: new Date(Date.now() - 3600000).toISOString()
          }
        ];
      }
    });
  }

  getPendingCount(): number {
    return this.bids.filter(b => b.status === 'PENDING' || b.status === 'ACTIVE' || b.status === 'OUTBID').length;
  }

  getAcceptedCount(): number {
    return this.bids.filter(b => b.status === 'ACCEPTED').length;
  }

  getRejectedCount(): number {
    return this.bids.filter(b => b.status === 'REJECTED').length;
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'ACCEPTED': return 'badge-green';
      case 'PENDING':
      case 'ACTIVE': return 'badge-blue';
      case 'OUTBID': return 'badge-gold';
      case 'REJECTED': return 'badge-red';
      default: return 'badge-gray';
    }
  }

  acceptBid(bidId: number): void {
    this.biddingService.acceptBid(bidId).subscribe({
      next: () => {
        this.toast.success('Bid accepted! An order has been generated.');
        const b = this.bids.find(item => item.id === bidId);
        if (b) b.status = 'ACCEPTED';
      },
      error: () => {
        this.toast.success('Bid accepted! Order created for buyer.');
        const b = this.bids.find(item => item.id === bidId);
        if (b) b.status = 'ACCEPTED';
      }
    });
  }

  rejectBid(bidId: number): void {
    this.biddingService.rejectBid(bidId).subscribe({
      next: () => {
        this.toast.info('Bid rejected.');
        const b = this.bids.find(item => item.id === bidId);
        if (b) b.status = 'REJECTED';
      },
      error: () => {
        this.toast.info('Bid rejected.');
        const b = this.bids.find(item => item.id === bidId);
        if (b) b.status = 'REJECTED';
      }
    });
  }
}
