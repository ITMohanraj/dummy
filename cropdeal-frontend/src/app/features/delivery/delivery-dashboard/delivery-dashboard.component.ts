import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { DeliveryService } from '../../../core/services/delivery.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { DeliveryRequest, DeliveryStatus } from '../../../core/models/delivery.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-delivery-dashboard',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    LoadingSpinnerComponent, 
    EmptyStateComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="delivery-dashboard py-8">
      <div class="container">
        
        <!-- Welcome Banner -->
        <div class="dash-welcome card p-6 mb-8 flex justify-between items-center flex-wrap gap-4">
          <div>
            <span class="badge badge-blue mb-1">Logistics & Fleet Hub</span>
            <h1 class="text-2xl font-extrabold text-dark">Welcome, {{ authService.currentUser()?.fullName }}</h1>
            <p class="text-sm text-muted">Accept farm pickup trips, track transit routes, and receive automated ₹10/km escrow payouts.</p>
          </div>

          <div class="flex gap-3">
            <a routerLink="/wallet" class="btn btn-secondary btn-sm">
              <span class="material-symbols-outlined text-sm">account_balance_wallet</span>
              Trip Earnings
            </a>
          </div>
        </div>

        <!-- Section 1: Open Delivery Jobs Marketplace -->
        <div class="card p-6 mb-8">
          <div class="flex justify-between items-center mb-4">
            <div>
              <h2 class="font-bold text-lg text-dark">Available Open Farm Consignments</h2>
              <p class="text-xs text-muted">Accept open delivery requests. Standard fleet compensation is ₹10 per KM.</p>
            </div>
            <button class="btn btn-secondary btn-sm" (click)="loadAvailableJobs()">
              <span class="material-symbols-outlined text-xs">refresh</span>
              Refresh Jobs
            </button>
          </div>

          <app-loading-spinner *ngIf="loadingJobs" message="Scanning open farm delivery routes..."></app-loading-spinner>

          <div *ngIf="!loadingJobs && openJobs.length > 0" class="jobs-grid">
            <div *ngFor="let job of openJobs" class="job-card card p-5 border-l-4 border-emerald-500">
              <div class="flex justify-between items-start mb-3">
                <div>
                  <span class="badge badge-green text-2xs">TRIP #{{ job.deliveryId }}</span>
                  <div class="text-xs text-muted mt-0.5">Order Ref #{{ job.orderId }}</div>
                </div>
                <div class="fee-badge text-emerald-800 font-extrabold text-lg">
                  {{ job.deliveryFee | inr }}
                </div>
              </div>

              <div class="route-details my-3">
                <div class="route-point flex items-center gap-2 mb-2">
                  <span class="material-symbols-outlined text-emerald-600 text-sm">trip_origin</span>
                  <div class="text-xs"><strong>Pickup:</strong> {{ job.pickupAddress }}</div>
                </div>
                <div class="route-point flex items-center gap-2">
                  <span class="material-symbols-outlined text-red-500 text-sm">location_on</span>
                  <div class="text-xs"><strong>Drop:</strong> {{ job.deliveryAddress }}</div>
                </div>
              </div>

              <div class="flex justify-between items-center pt-3 border-t border-slate-100 text-xs text-muted">
                <span>Distance: <strong>{{ job.distanceKm }} KM</strong></span>
                <span>Load: <strong>{{ job.weightKg }} KG</strong></span>
              </div>

              <button 
                class="btn btn-primary btn-sm w-full mt-4"
                (click)="acceptJob(job.deliveryId)"
              >
                <span class="material-symbols-outlined text-xs">check_circle</span>
                Accept Consignment Trip
              </button>
            </div>
          </div>

          <app-empty-state 
            *ngIf="!loadingJobs && openJobs.length === 0"
            icon="local_shipping"
            title="No Open Delivery Requests"
            description="All current dealer shipments have been assigned. Check back shortly for new farm routes."
          ></app-empty-state>
        </div>

        <!-- Section 2: Active Assigned Consignments & Progress -->
        <div class="card p-6">
          <h2 class="font-bold text-lg text-dark mb-4">Active & In-Transit Consignments</h2>

          <div *ngIf="activeDeliveries.length === 0" class="text-sm text-muted py-6 text-center">
            You do not have any active shipments in transit right now.
          </div>

          <div *ngIf="activeDeliveries.length > 0" class="table-container">
            <table class="custom-table">
              <thead>
                <tr>
                  <th>Delivery ID</th>
                  <th>Route</th>
                  <th>Distance & Fee</th>
                  <th>Current State</th>
                  <th>Lifecycle Actions</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let d of activeDeliveries">
                  <td>#{{ d.deliveryId }}</td>
                  <td>
                    <div class="text-xs"><strong>From:</strong> {{ d.pickupAddress }}</div>
                    <div class="text-xs text-muted"><strong>To:</strong> {{ d.deliveryAddress }}</div>
                  </td>
                  <td>
                    <div class="font-bold text-emerald-800">{{ d.deliveryFee | inr }}</div>
                    <div class="text-2xs text-muted">{{ d.distanceKm }} KM</div>
                  </td>
                  <td>
                    <span class="badge" [ngClass]="d.status === 'DELIVERED' ? 'badge-green' : 'badge-blue'">
                      {{ d.status }}
                    </span>
                  </td>
                  <td>
                    <div class="flex gap-2">
                      <button 
                        *ngIf="d.status === 'ACCEPTED' || d.status === 'ASSIGNED'"
                        class="btn btn-secondary btn-sm text-xs"
                        (click)="updateStatus(d.deliveryId, 'PICKED_UP')"
                      >
                        Mark Picked Up
                      </button>

                      <button 
                        *ngIf="d.status === 'PICKED_UP'"
                        class="btn btn-secondary btn-sm text-xs"
                        (click)="updateStatus(d.deliveryId, 'IN_TRANSIT')"
                      >
                        In Transit
                      </button>

                      <button 
                        *ngIf="d.status === 'IN_TRANSIT'"
                        class="btn btn-primary btn-sm text-xs"
                        (click)="updateStatus(d.deliveryId, 'DELIVERED')"
                      >
                        Mark Delivered & Claim Payout
                      </button>
                    </div>
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
    .jobs-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1.25rem;
    }
    .text-2xs { font-size: 0.625rem; }
  `]
})
export class DeliveryDashboardComponent implements OnInit {
  private deliveryService = inject(DeliveryService);
  authService = inject(AuthService);
  private toast = inject(ToastService);

  loadingJobs = true;
  openJobs: DeliveryRequest[] = [];
  activeDeliveries: DeliveryRequest[] = [];

  ngOnInit(): void {
    this.loadAvailableJobs();
  }

  loadAvailableJobs(): void {
    this.loadingJobs = true;
    this.deliveryService.getAvailableDeliveries().subscribe({
      next: (jobs) => {
        this.openJobs = jobs.filter(j => j.status === 'PENDING');
        this.activeDeliveries = jobs.filter(j => j.status !== 'PENDING');
        this.loadingJobs = false;
      },
      error: () => {
        this.loadingJobs = false;
      }
    });
  }

  acceptJob(deliveryId: number): void {
    const partnerId = this.authService.getUserId() || 301;
    this.deliveryService.acceptDelivery(deliveryId, partnerId).subscribe({
      next: (res) => {
        this.toast.success(`Consignment #${res.deliveryId} accepted! Proceed to farm pickup location.`);
        this.loadAvailableJobs();
      },
      error: () => {}
    });
  }

  updateStatus(deliveryId: number, status: DeliveryStatus): void {
    this.deliveryService.updateDeliveryStatus(deliveryId, status).subscribe({
      next: (res) => {
        if (status === 'DELIVERED') {
          this.toast.success(`Delivery #${res.deliveryId} completed! Delivery transport fee released to your wallet.`);
        } else {
          this.toast.info(`Delivery status updated to: ${status}`);
        }
        this.loadAvailableJobs();
      },
      error: () => {}
    });
  }
}
