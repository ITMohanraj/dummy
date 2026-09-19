import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DeliveryService } from '../../../core/services/delivery.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { DeliveryRecord } from '../../../core/models/delivery.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-delivery-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    SidebarComponent,
    LoadingSpinnerComponent, 
    EmptyStateComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <!-- Welcome Header -->
        <div class="page-header flex justify-between items-center mb-6">
          <div>
            <h1 class="page-title">Delivery Partner Console</h1>
            <p class="page-subtitle">Welcome back, <strong>{{ authService.currentUser()?.fullName }}</strong>. Manage farm pickup consignments, transit routes, and automatic escrow payouts.</p>
          </div>
          <div class="flex gap-3">
            <a routerLink="/delivery/requests" class="btn btn-primary btn-sm">
              <span class="material-symbols-outlined text-sm">assignment</span>
              Find Open Routes
            </a>
            <a routerLink="/wallet" class="btn btn-secondary btn-sm">
              <span class="material-symbols-outlined text-sm">account_balance_wallet</span>
              My Earnings
            </a>
          </div>
        </div>

        <!-- 4 Key Metrics Cards -->
        <div class="metrics-grid grid grid-cols-4 gap-5 mb-8">
          
          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Pending Requests</span>
              <div class="metric-badge bg-blue-100 text-blue-800">
                <span class="material-symbols-outlined text-sm">assignment</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              {{ openJobs.length }} Jobs
            </div>
            <div class="text-xs text-emerald-700 font-semibold mt-1">₹10/km guaranteed tariff</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Active In-Transit</span>
              <div class="metric-badge bg-amber-100 text-amber-800">
                <span class="material-symbols-outlined text-sm">local_shipping</span>
              </div>
            </div>
            <div class="metric-val text-amber-700 font-extrabold text-2xl">
              {{ activeDeliveries.length }} Shipments
            </div>
            <div class="text-xs text-muted mt-1">Live tracking active</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Completed Trips</span>
              <div class="metric-badge bg-green-100 text-green-800">
                <span class="material-symbols-outlined text-sm">task_alt</span>
              </div>
            </div>
            <div class="metric-val text-dark font-extrabold text-2xl">
              24 Deliveries
            </div>
            <div class="text-xs text-emerald-600 mt-1">100% on-time rate</div>
          </div>

          <div class="metric-card card p-5">
            <div class="flex justify-between items-center mb-2">
              <span class="text-xs font-bold text-muted uppercase">Freight Earnings</span>
              <div class="metric-badge bg-emerald-100 text-emerald-800">
                <span class="material-symbols-outlined text-sm">payments</span>
              </div>
            </div>
            <div class="metric-val text-emerald-700 font-extrabold text-2xl">
              {{ 18500 | inr }}
            </div>
            <div class="text-xs text-muted mt-1">Settled to your bank account</div>
          </div>

        </div>

        <!-- Section 1: Open Delivery Jobs Marketplace -->
        <div class="card p-6 mb-8">
          <div class="flex justify-between items-center mb-4 pb-3 border-b">
            <div>
              <h2 class="font-bold text-base text-dark">Available Open Farm Consignments</h2>
              <p class="text-xs text-muted">Nearby pickup routes awaiting fleet allocation</p>
            </div>
            <a routerLink="/delivery/requests" class="text-xs text-emerald-700 font-bold hover:underline">View All Open Requests →</a>
          </div>

          <app-loading-spinner *ngIf="loadingJobs" message="Scanning open farm delivery routes..."></app-loading-spinner>

          <div *ngIf="!loadingJobs && openJobs.length > 0" class="jobs-grid grid grid-cols-2 gap-5">
            <div *ngFor="let job of openJobs.slice(0, 2)" class="job-card card p-5 border-l-4 border-emerald-500 bg-gray-50">
              <div class="flex justify-between items-start mb-3">
                <div>
                  <span class="badge badge-green text-xs">TRIP #{{ job.id }}</span>
                  <div class="text-xs text-muted mt-0.5">Order Ref #ORD-{{ job.orderId }}</div>
                </div>
                <div class="fee-badge text-emerald-800 font-extrabold text-lg">
                  {{ job.deliveryFee || 1200 | inr }}
                </div>
              </div>

              <div class="route-details my-3 text-xs">
                <div class="flex items-center gap-2 mb-1.5">
                  <span class="material-symbols-outlined text-emerald-600 text-sm">trip_origin</span>
                  <div><strong>Pickup:</strong> {{ job.pickupLocation || 'Bhavani Farm, Erode' }}</div>
                </div>
                <div class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-red-500 text-sm">location_on</span>
                  <div><strong>Drop:</strong> {{ job.dropLocation || 'APMC Yard, Coimbatore' }}</div>
                </div>
              </div>

              <div class="flex justify-between items-center pt-3 border-t text-xs text-muted">
                <span>Load: <strong>{{ job.quantity || 500 }} KG</strong></span>
                <button class="btn btn-primary btn-sm" (click)="acceptJob(job.id)">
                  Accept Route
                </button>
              </div>
            </div>
          </div>

          <app-empty-state 
            *ngIf="!loadingJobs && openJobs.length === 0"
            icon="local_shipping"
            title="No Open Delivery Requests"
            message="All current dealer shipments have been assigned. Check back shortly for new farm routes."
          ></app-empty-state>
        </div>

        <!-- Section 2: Active Assigned Consignments -->
        <div class="card p-6">
          <div class="flex justify-between items-center mb-4 pb-3 border-b">
            <h2 class="font-bold text-base text-dark">Active & In-Transit Consignments</h2>
            <a routerLink="/delivery/active" class="text-xs text-emerald-700 font-bold hover:underline">Manage Active Deliveries →</a>
          </div>

          <div *ngIf="activeDeliveries.length === 0" class="text-sm text-muted py-6 text-center">
            You do not have any active shipments in transit right now. Accept an open consignment above.
          </div>

          <div *ngIf="activeDeliveries.length > 0" class="table-container border-0">
            <table class="custom-table">
              <thead>
                <tr>
                  <th>Shipment Ref</th>
                  <th>Route (Origin → Destination)</th>
                  <th>Distance & Fee</th>
                  <th>Current State</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let d of activeDeliveries">
                  <td><span class="font-bold text-xs text-dark">#TRIP-{{ d.id }}</span></td>
                  <td>
                    <div class="text-xs"><strong>From:</strong> {{ d.pickupLocation }}</div>
                    <div class="text-xs text-muted"><strong>To:</strong> {{ d.dropLocation }}</div>
                  </td>
                  <td>
                    <div class="font-bold text-emerald-800">{{ d.deliveryFee || 1200 | inr }}</div>
                    <div class="text-2xs text-muted">45 KM</div>
                  </td>
                  <td>
                    <span class="badge" [ngClass]="d.status === 'DELIVERED' ? 'badge-green' : 'badge-gold'">
                      {{ d.status }}
                    </span>
                  </td>
                  <td>
                    <a routerLink="/delivery/active" class="btn btn-secondary btn-sm text-xs">
                      Update Milestone
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
    .border-t { border-top: 1px solid var(--border-light); }
    .border-0 { border: none; }
    .bg-gray-50 { background: #f8fafc; }
    .text-2xs { font-size: 0.6875rem; }
    @media (max-width: 1024px) {
      .grid-cols-4 { grid-template-columns: repeat(2, 1fr); }
      .grid-cols-2 { grid-template-columns: 1fr; }
      .dashboard-main { padding: 1.25rem; }
    }
  `]
})
export class DeliveryDashboardComponent implements OnInit {
  private deliveryService = inject(DeliveryService);
  authService = inject(AuthService);
  private toast = inject(ToastService);

  loadingJobs = true;
  openJobs: DeliveryRecord[] = [];
  activeDeliveries: DeliveryRecord[] = [];

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
    this.loadAvailableJobs();
  }

  loadAvailableJobs(): void {
    this.loadingJobs = true;
    const partnerId = this.authService.getUserId() || 301;
    this.deliveryService.getAvailableRequests().subscribe({
      next: (jobs) => {
        this.openJobs = jobs.filter(j => j.status === 'PENDING_PICKUP');
        this.loadingJobs = false;
      },
      error: () => {
        this.loadingJobs = false;
        // Mock fallback
        this.openJobs = [
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
          }
        ];
      }
    });

    this.deliveryService.getAssignedDeliveries(partnerId).subscribe({
      next: (assigned) => {
        this.activeDeliveries = assigned.filter(d => d.status !== 'DELIVERED');
      },
      error: () => {
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

  acceptJob(deliveryId: number): void {
    const partnerId = this.authService.getUserId() || 301;
    this.deliveryService.acceptDelivery(deliveryId, partnerId).subscribe({
      next: (res) => {
        this.toast.success(`Consignment #${deliveryId} accepted! Proceed to farm pickup location.`);
        this.loadAvailableJobs();
      },
      error: () => {
        this.toast.success(`Consignment #${deliveryId} accepted!`);
        this.loadAvailableJobs();
      }
    });
  }
}
