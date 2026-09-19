import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { OrderService } from '../../../core/services/order.service';
import { DeliveryService } from '../../../core/services/delivery.service';
import { InvoiceService } from '../../../core/services/invoice.service';
import { ReviewService } from '../../../core/services/review.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { OrderResponse } from '../../../core/models/order.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-dealer-orders',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule, 
    LoadingSpinnerComponent, 
    EmptyStateComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="dealer-orders-page py-8">
      <div class="container">
        
        <div class="flex justify-between items-center mb-6">
          <div>
            <h1 class="text-2xl font-extrabold text-dark">My Purchase Orders & Logistics</h1>
            <p class="text-xs text-muted">Track order status, assign delivery partners, download invoices, and review farmers.</p>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading your purchase orders..."></app-loading-spinner>

        <div *ngIf="!loading && orders.length > 0" class="table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Produce Consignment</th>
                <th>Farmer</th>
                <th>Quantity</th>
                <th>Total Value</th>
                <th>Order Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let o of orders">
                <td><span class="font-bold text-dark">#{{ o.orderId }}</span></td>
                <td>
                  <span class="font-bold text-emerald-800">{{ o.cropName }}</span>
                </td>
                <td>Farmer #{{ o.farmerId }}</td>
                <td>{{ o.quantityKg }} KG</td>
                <td><strong>{{ o.totalAmount | inr }}</strong></td>
                <td>
                  <span class="badge" [ngClass]="o.status === 'DELIVERED' || o.status === 'COMPLETED' ? 'badge-green' : 'badge-gold'">
                    {{ o.status }}
                  </span>
                </td>
                <td>
                  <div class="flex gap-2 flex-wrap">
                    <!-- Assign Delivery Partner -->
                    <button 
                      class="btn btn-secondary btn-sm text-xs"
                      (click)="openDeliveryModal(o)"
                      title="Request Delivery Partner"
                    >
                      <span class="material-symbols-outlined text-xs">local_shipping</span>
                      Logistics
                    </button>

                    <!-- Download Invoice PDF -->
                    <button 
                      class="btn btn-secondary btn-sm text-xs"
                      (click)="downloadInvoice(o.orderId)"
                      title="Download Tax Invoice PDF"
                    >
                      <span class="material-symbols-outlined text-xs">download</span>
                      Invoice
                    </button>

                    <!-- Review Farmer -->
                    <button 
                      class="btn btn-primary btn-sm text-xs"
                      (click)="openReviewModal(o)"
                      title="Review Farmer"
                    >
                      <span class="material-symbols-outlined text-xs">star</span>
                      Review
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <app-empty-state 
          *ngIf="!loading && orders.length === 0"
          icon="shopping_basket"
          title="No Purchases Yet"
          description="Explore our marketplace to buy fresh crops directly from farmers."
          actionLabel="Browse Marketplace"
          (actionClicked)="router.navigate(['/crops'])"
        ></app-empty-state>

        <!-- Assign Delivery Partner Modal -->
        <div *ngIf="deliveryTargetOrder" class="modal-backdrop">
          <div class="modal-card card p-6">
            <h3 class="font-bold text-lg mb-2">Assign Delivery Partner for Order #{{ deliveryTargetOrder.orderId }}</h3>
            <p class="text-xs text-muted mb-4">Transport rate is calculated at ₹10 per KM.</p>

            <div class="form-group">
              <label class="form-label">Pickup Address (Farm)</label>
              <input type="text" [(ngModel)]="pickupAddress" class="form-control" />
            </div>

            <div class="form-group">
              <label class="form-label">Delivery Destination Address</label>
              <input type="text" [(ngModel)]="deliveryAddress" class="form-control" />
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div class="form-group">
                <label class="form-label">Distance (KM)</label>
                <input type="number" [(ngModel)]="distanceKm" (input)="calcDeliveryFee()" class="form-control" />
              </div>
              <div class="form-group">
                <label class="form-label">Estimated Transport Fee</label>
                <div class="font-extrabold text-lg text-emerald-800 pt-2">{{ estimatedFee | inr }}</div>
              </div>
            </div>

            <div class="flex justify-end gap-3 mt-6">
              <button class="btn btn-secondary btn-sm" (click)="deliveryTargetOrder = undefined">Cancel</button>
              <button class="btn btn-primary btn-sm" (click)="submitDeliveryRequest()">Confirm Logistics Request</button>
            </div>
          </div>
        </div>

        <!-- Review Modal -->
        <div *ngIf="reviewTargetOrder" class="modal-backdrop">
          <div class="modal-card card p-6">
            <h3 class="font-bold text-lg mb-2">Review Farmer #{{ reviewTargetOrder.farmerId }}</h3>
            <p class="text-xs text-muted mb-4">Rate crop quality, packaging, and fulfillment experience.</p>

            <div class="form-group">
              <label class="form-label">Rating (1 to 5 Stars)</label>
              <select [(ngModel)]="reviewRating" class="form-control">
                <option [value]="5">★★★★★ 5.0 - Excellent Quality & Prompt Fulfillment</option>
                <option [value]="4">★★★★☆ 4.0 - Good Produce</option>
                <option [value]="3">★★★☆☆ 3.0 - Satisfactory</option>
                <option [value]="2">★★☆☆☆ 2.0 - Below Expectations</option>
                <option [value]="1">★☆☆☆☆ 1.0 - Poor Quality</option>
              </select>
            </div>

            <div class="form-group">
              <label class="form-label">Detailed Comments</label>
              <textarea [(ngModel)]="reviewComment" rows="3" class="form-control" placeholder="Crop freshness was as described..."></textarea>
            </div>

            <div class="flex justify-end gap-3 mt-6">
              <button class="btn btn-secondary btn-sm" (click)="reviewTargetOrder = undefined">Cancel</button>
              <button class="btn btn-primary btn-sm" (click)="submitReview()">Submit Review</button>
            </div>
          </div>
        </div>

      </div>
    </div>
  `,
  styles: [`
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.6);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2000;
    }
    .modal-card {
      width: 100%;
      max-width: 480px;
    }
  `]
})
export class DealerOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private deliveryService = inject(DeliveryService);
  private invoiceService = inject(InvoiceService);
  private reviewService = inject(ReviewService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);
  router = inject(Router);

  orders: OrderResponse[] = [];
  loading = true;

  deliveryTargetOrder?: OrderResponse;
  pickupAddress = 'Salem Rural Farmer Hub, Tamil Nadu';
  deliveryAddress = 'Kisan Mandi Yard, Bangalore East';
  distanceKm = 45;
  estimatedFee = 450;

  reviewTargetOrder?: OrderResponse;
  reviewRating = 5;
  reviewComment = 'Excellent quality produce, exactly matching the Mandi grade specification.';

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    const dealerId = this.authService.getUserId() || 201;
    this.loading = true;
    this.orderService.getOrdersByDealer(dealerId).subscribe({
      next: (res) => {
        this.orders = res || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  openDeliveryModal(order: OrderResponse): void {
    this.deliveryTargetOrder = order;
    this.calcDeliveryFee();
  }

  calcDeliveryFee(): void {
    this.estimatedFee = (this.distanceKm || 0) * 10;
  }

  submitDeliveryRequest(): void {
    if (!this.deliveryTargetOrder) return;

    this.deliveryService.createDeliveryRequest({
      orderId: this.deliveryTargetOrder.orderId,
      dealerId: this.deliveryTargetOrder.dealerId,
      farmerId: this.deliveryTargetOrder.farmerId,
      pickupAddress: this.pickupAddress,
      deliveryAddress: this.deliveryAddress,
      distanceKm: this.distanceKm,
      weightKg: this.deliveryTargetOrder.quantityKg,
      notes: 'Handle with care. Perishable agricultural produce.'
    }).subscribe({
      next: (req) => {
        this.toast.success(`Delivery request #${req.deliveryId} created! Published to Delivery Partner network.`);
        this.deliveryTargetOrder = undefined;
      },
      error: () => {}
    });
  }

  downloadInvoice(orderId: number): void {
    this.invoiceService.downloadInvoicePdf(orderId).subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `Invoice-Order-${orderId}.pdf`;
        a.click();
        window.URL.revokeObjectURL(url);
        this.toast.success('Tax Invoice PDF downloaded!');
      },
      error: () => {
        this.toast.info('Generated tax invoice summary.');
      }
    });
  }

  openReviewModal(order: OrderResponse): void {
    this.reviewTargetOrder = order;
  }

  submitReview(): void {
    if (!this.reviewTargetOrder) return;

    this.reviewService.postReview({
      orderId: this.reviewTargetOrder.orderId,
      dealerId: this.reviewTargetOrder.dealerId,
      farmerId: this.reviewTargetOrder.farmerId,
      rating: +this.reviewRating,
      comment: this.reviewComment
    }).subscribe({
      next: () => {
        this.toast.success('Review submitted successfully! Thank you for rating the farmer.');
        this.reviewTargetOrder = undefined;
      },
      error: () => {}
    });
  }
}
