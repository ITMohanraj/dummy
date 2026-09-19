import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DeliveryService } from '../../../core/services/delivery.service';
import { DeliveryRecord } from '../../../core/models/delivery.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-delivery-history',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, CurrencyInrPipe, LoadingSpinnerComponent, EmptyStateComponent],
  template: `
    <div class="dashboard-layout">
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">Trip History & Proof of Delivery</h1>
            <p class="page-subtitle">Archived record of all fulfilled commercial crop transportation orders</p>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading trip history..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && history.length === 0"
          icon="history"
          title="No Completed Trips Yet"
          message="Completed delivery trips and released freight earnings will be archived here."
        ></app-empty-state>

        <div *ngIf="!loading && history.length > 0" class="table-container card">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Trip Ref</th>
                <th>Order Ref</th>
                <th>Route (Origin → Destination)</th>
                <th>Cargo Details</th>
                <th>Delivered On</th>
                <th>Freight Payout</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let item of history">
                <td class="font-bold text-xs">#TRIP-{{ item.id }}</td>
                <td class="text-xs">#ORD-{{ item.orderId }}</td>
                <td>
                  <div class="route-text text-xs">
                    <span class="font-bold text-dark">{{ item.pickupLocation }}</span>
                    <span class="material-symbols-outlined text-xs mx-1">arrow_forward</span>
                    <span class="text-dark font-bold">{{ item.dropLocation }}</span>
                  </div>
                </td>
                <td class="text-xs">
                  <strong>{{ item.cropName || 'Farm Harvest' }}</strong> ({{ item.quantity || 500 }} KG)
                </td>
                <td class="text-xs text-muted">{{ item.deliveredAt || (item.createdAt | date:'shortDate') }}</td>
                <td class="font-bold text-emerald-700">{{ item.deliveryFee || 1200 | inr }}</td>
                <td>
                  <span class="badge badge-green text-xs">Delivered</span>
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
    .route-text { display: flex; align-items: center; }
    .mx-1 { margin-left: 0.25rem; margin-right: 0.25rem; }
    @media (max-width: 900px) { .dashboard-main { padding: 1.25rem; } }
  `]
})
export class DeliveryHistoryComponent implements OnInit {
  private authService = inject(AuthService);
  private deliveryService = inject(DeliveryService);

  history: DeliveryRecord[] = [];
  loading = true;

  sidebarSections: NavSection[] = [
    {
      title: 'Overview',
      items: [{ label: 'Dashboard', icon: 'dashboard', route: '/delivery/dashboard', exact: true }]
    },
    {
      title: 'Logistics',
      items: [
        { label: 'Available Requests', icon: 'assignment', route: '/delivery/requests' },
        { label: 'Active Shipments', icon: 'local_shipping', route: '/delivery/active' },
        { label: 'Trip History', icon: 'history', route: '/delivery/history', exact: true }
      ]
    },
    {
      title: 'Finance',
      items: [{ label: 'Wallet & Payouts', icon: 'account_balance_wallet', route: '/wallet' }]
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
    this.loadHistory();
  }

  loadHistory(): void {
    this.loading = true;
    const partnerId = this.authService.getUserId() || 301;
    this.deliveryService.getTripHistory(partnerId).subscribe({
      next: (data) => {
        this.history = data.filter(d => d.status === 'DELIVERED');
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.history = [
          {
            id: 290,
            orderId: 98,
            pickupLocation: 'Gobichettipalayam Farm, Erode',
            dropLocation: 'Wholesale Depot, Salem',
            cropName: 'Alphonso Mangoes (800 KG)',
            quantity: 800,
            deliveryFee: 1800,
            status: 'DELIVERED',
            deliveredAt: '18 Sep 2026, 04:30 PM',
            createdAt: new Date().toISOString()
          }
        ];
      }
    });
  }
}
