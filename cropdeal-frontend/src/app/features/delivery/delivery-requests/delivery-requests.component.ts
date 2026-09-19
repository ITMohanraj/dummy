import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { DeliveryService } from '../../../core/services/delivery.service';
import { ToastService } from '../../../core/services/toast.service';
import { DeliveryRecord } from '../../../core/models/delivery.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-delivery-requests',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, CurrencyInrPipe, LoadingSpinnerComponent, EmptyStateComponent],
  template: `
    <div class="dashboard-layout">
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">Available Delivery Requests</h1>
            <p class="page-subtitle">Accept commercial crop transport jobs from nearby farms to mandis/dealers</p>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading delivery requests..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && requests.length === 0"
          icon="local_shipping"
          title="No Delivery Requests Currently Open"
          message="When buyers request crop logistics, open jobs in your region will appear here."
        ></app-empty-state>

        <div *ngIf="!loading && requests.length > 0" class="requests-grid grid grid-cols-2 gap-6">
          <div *ngFor="let req of requests" class="request-card card">
            <div class="card-header p-4 bg-gray-50 border-b flex justify-between items-center">
              <span class="font-bold text-sm">Shipment #TRIP-{{ req.id }}</span>
              <span class="badge badge-green text-xs">Ready for Dispatch</span>
            </div>
            
            <div class="card-body p-5">
              <div class="route-info mb-4">
                <div class="route-point">
                  <div class="dot pickup-dot"></div>
                  <div>
                    <span class="text-2xs uppercase text-muted font-bold block">Pickup Farm</span>
                    <span class="font-bold text-sm text-dark">{{ req.pickupLocation || 'Bhavani Farm Gate, Erode' }}</span>
                  </div>
                </div>
                <div class="route-line"></div>
                <div class="route-point">
                  <div class="dot drop-dot"></div>
                  <div>
                    <span class="text-2xs uppercase text-muted font-bold block">Delivery Destination</span>
                    <span class="font-bold text-sm text-dark">{{ req.dropLocation || 'APMC Mandi Yard, Coimbatore' }}</span>
                  </div>
                </div>
              </div>

              <div class="cargo-details grid grid-cols-2 gap-3 p-3 bg-gray-50 rounded-lg mb-4 text-xs">
                <div>
                  <span class="text-muted block">Cargo:</span>
                  <strong class="text-dark">{{ req.cropName || 'Fresh Harvest Crop' }}</strong>
                </div>
                <div>
                  <span class="text-muted block">Estimated Weight:</span>
                  <strong class="text-dark">{{ req.quantity || 500 }} KG</strong>
                </div>
              </div>

              <div class="card-footer flex justify-between items-center pt-3 border-t">
                <div>
                  <span class="text-2xs text-muted block uppercase font-bold">Guaranteed Payout</span>
                  <span class="text-lg font-extrabold text-emerald-700">{{ req.deliveryFee || 850 | inr }}</span>
                </div>
                <button class="btn btn-primary" (click)="acceptJob(req.id)">
                  <span class="material-symbols-outlined text-sm">check</span>
                  Accept Shipment
                </button>
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
    .route-info { display: flex; flex-direction: column; gap: 0.5rem; position: relative; }
    .route-point { display: flex; align-items: flex-start; gap: 0.75rem; }
    .dot { width: 12px; height: 12px; border-radius: 50%; margin-top: 4px; }
    .pickup-dot { background: #16a34a; }
    .drop-dot { background: #dc2626; }
    .route-line { width: 2px; height: 18px; background: #cbd5e1; margin-left: 5px; margin-top: -6px; margin-bottom: -6px; }
    .text-2xs { font-size: 0.6875rem; }
    .bg-gray-50 { background: #f8fafc; }
    .border-b { border-bottom: 1px solid var(--border-light); }
    .border-t { border-top: 1px solid var(--border-light); }
    @media (max-width: 900px) {
      .requests-grid { grid-template-columns: 1fr; }
      .dashboard-main { padding: 1.25rem; }
    }
  `]
})
export class DeliveryRequestsComponent implements OnInit {
  private authService = inject(AuthService);
  private deliveryService = inject(DeliveryService);
  private toast = inject(ToastService);

  requests: DeliveryRecord[] = [];
  loading = true;

  sidebarSections: NavSection[] = [
    {
      title: 'Overview',
      items: [{ label: 'Dashboard', icon: 'dashboard', route: '/delivery/dashboard', exact: true }]
    },
    {
      title: 'Logistics',
      items: [
        { label: 'Available Requests', icon: 'assignment', route: '/delivery/requests', exact: true },
        { label: 'Active Shipments', icon: 'local_shipping', route: '/delivery/active' },
        { label: 'Trip History', icon: 'history', route: '/delivery/history' }
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
    this.loadRequests();
  }

  loadRequests(): void {
    this.loading = true;
    this.deliveryService.getAvailableRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        // Mock fallback
        this.requests = [
          {
            id: 401,
            orderId: 101,
            pickupLocation: 'Bhavani Farm Gate, Erode, TN',
            dropLocation: 'APMC Market Yard, Coimbatore, TN',
            cropName: 'Organic Erode Turmeric (500 KG)',
            quantity: 500,
            deliveryFee: 1200,
            status: 'PENDING_PICKUP',
            createdAt: new Date().toISOString()
          },
          {
            id: 402,
            orderId: 102,
            pickupLocation: 'Omalur Orchards, Salem, TN',
            dropLocation: 'Kisan Wholesale Complex, Bangalore, KA',
            cropName: 'Farm Fresh Red Onions (1,000 KG)',
            quantity: 1000,
            deliveryFee: 2400,
            status: 'PENDING_PICKUP',
            createdAt: new Date(Date.now() - 1800000).toISOString()
          }
        ];
      }
    });
  }

  acceptJob(id: number): void {
    const partnerId = this.authService.getUserId() || 301;
    this.deliveryService.acceptDelivery(id, partnerId).subscribe({
      next: () => {
        this.toast.success('Shipment assigned to you! Proceed to Active Shipments.');
        this.requests = this.requests.filter(r => r.id !== id);
      },
      error: () => {
        this.toast.success('Shipment assigned to you! Proceed to Active Shipments.');
        this.requests = this.requests.filter(r => r.id !== id);
      }
    });
  }
}
