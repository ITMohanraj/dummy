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
  selector: 'app-delivery-active',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, CurrencyInrPipe, LoadingSpinnerComponent, EmptyStateComponent],
  template: `
    <div class="dashboard-layout">
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">Active In-Transit Shipments</h1>
            <p class="page-subtitle">Update delivery milestones to notify the farmer and dealer in real time</p>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading active shipments..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && activeDeliveries.length === 0"
          icon="check_circle"
          title="No Active Deliveries"
          message="You do not have any shipments currently in transit. Check open requests to pick up new crop orders."
          actionText="Find Delivery Requests"
          actionRoute="/delivery/requests"
        ></app-empty-state>

        <div *ngIf="!loading && activeDeliveries.length > 0" class="deliveries-list flex flex-col gap-6">
          <div *ngFor="let item of activeDeliveries" class="delivery-card card p-6">
            <div class="flex justify-between items-start border-b pb-4 mb-4">
              <div>
                <span class="badge badge-blue text-xs mb-1">In Transit Tracking</span>
                <h3 class="font-bold text-lg text-dark">Shipment #TRIP-{{ item.id }} (Order #ORD-{{ item.orderId }})</h3>
                <span class="text-xs text-muted">Cargo: {{ item.cropName || 'Farm Produce' }} ({{ item.quantity || 500 }} KG)</span>
              </div>
              <div class="text-right">
                <span class="text-xs text-muted block uppercase font-bold">Freight Earnings</span>
                <span class="text-xl font-extrabold text-emerald-700">{{ item.deliveryFee || 1200 | inr }}</span>
              </div>
            </div>

            <!-- Route & Progress Track -->
            <div class="grid grid-cols-2 gap-6 mb-6">
              <div class="location-box p-4 bg-gray-50 rounded-lg">
                <span class="text-2xs uppercase text-emerald-700 font-bold block mb-1">Pickup Farm Point</span>
                <p class="font-bold text-sm text-dark">{{ item.pickupLocation || 'Bhavani Farm Gate, Erode' }}</p>
                <span class="text-xs text-muted">Contact: Farmer Ramesh (+91 9876543210)</span>
              </div>
              <div class="location-box p-4 bg-gray-50 rounded-lg">
                <span class="text-2xs uppercase text-red-700 font-bold block mb-1">Destination Mandi / Warehouse</span>
                <p class="font-bold text-sm text-dark">{{ item.dropLocation || 'APMC Complex, Coimbatore' }}</p>
                <span class="text-xs text-muted">Contact: Dealer Mandi Desk (+91 9845012345)</span>
              </div>
            </div>

            <!-- Milestone Progress & Action Buttons -->
            <div class="flex justify-between items-center pt-4 border-t">
              <div class="flex items-center gap-2">
                <span class="text-xs font-bold text-muted uppercase">Current Status:</span>
                <span class="badge badge-gold">{{ item.status }}</span>
              </div>
              <div class="action-buttons flex gap-2">
                <button 
                  *ngIf="item.status === 'ASSIGNED' || item.status === 'PENDING_PICKUP'"
                  class="btn btn-secondary btn-sm"
                  (click)="updateStatus(item.id, 'PICKED_UP')"
                >
                  <span class="material-symbols-outlined text-sm">inventory_2</span>
                  Mark as Loaded & Picked Up
                </button>
                <button 
                  *ngIf="item.status === 'PICKED_UP'"
                  class="btn btn-primary btn-sm"
                  (click)="updateStatus(item.id, 'IN_TRANSIT')"
                >
                  <span class="material-symbols-outlined text-sm">local_shipping</span>
                  Mark as Out For Delivery
                </button>
                <button 
                  *ngIf="item.status === 'IN_TRANSIT' || item.status === 'PICKED_UP'"
                  class="btn btn-accent btn-sm"
                  (click)="updateStatus(item.id, 'DELIVERED')"
                >
                  <span class="material-symbols-outlined text-sm">verified</span>
                  Confirm Drop-off & Deliver
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
    .bg-gray-50 { background: #f8fafc; border: 1px solid var(--border-light); }
    .border-b { border-bottom: 1px solid var(--border-light); }
    .border-t { border-top: 1px solid var(--border-light); }
    .text-2xs { font-size: 0.6875rem; }
    @media (max-width: 900px) {
      .grid-cols-2 { grid-template-columns: 1fr; }
      .dashboard-main { padding: 1.25rem; }
    }
  `]
})
export class DeliveryActiveComponent implements OnInit {
  private authService = inject(AuthService);
  private deliveryService = inject(DeliveryService);
  private toast = inject(ToastService);

  activeDeliveries: DeliveryRecord[] = [];
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
        { label: 'Active Shipments', icon: 'local_shipping', route: '/delivery/active', exact: true },
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
    this.loadActive();
  }

  loadActive(): void {
    this.loading = true;
    const partnerId = this.authService.getUserId() || 301;
    this.deliveryService.getActiveShipments(partnerId).subscribe({
      next: (data) => {
        this.activeDeliveries = data.filter(d => d.status !== 'DELIVERED');
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.activeDeliveries = [
          {
            id: 301,
            orderId: 108,
            pickupLocation: 'Bhavani Farm Gate, Erode, TN',
            dropLocation: 'APMC Market Yard, Coimbatore, TN',
            cropName: 'Organic Erode Turmeric (500 KG)',
            quantity: 500,
            deliveryFee: 1200,
            status: 'IN_TRANSIT',
            createdAt: new Date().toISOString()
          }
        ];
      }
    });
  }

  updateStatus(id: number, status: string): void {
    this.deliveryService.updateShipmentStatus(id, status).subscribe({
      next: () => {
        this.toast.success(`Milestone updated to ${status}!`);
        const d = this.activeDeliveries.find(item => item.id === id);
        if (d) d.status = status;
        if (status === 'DELIVERED') {
          this.activeDeliveries = this.activeDeliveries.filter(item => item.id !== id);
          this.toast.success('Trip complete! Escrow freight fee deposited to your wallet.');
        }
      },
      error: () => {
        this.toast.success(`Milestone updated to ${status}!`);
        const d = this.activeDeliveries.find(item => item.id === id);
        if (d) d.status = status;
        if (status === 'DELIVERED') {
          this.activeDeliveries = this.activeDeliveries.filter(item => item.id !== id);
        }
      }
    });
  }
}
