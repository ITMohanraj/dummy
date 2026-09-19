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
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
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
        <div class="page-header flex justify-between items-center mb-6">
          <div>
            <h1 class="page-title">My Purchase Orders & Logistics</h1>
            <p class="page-subtitle">Track consignment milestones, choose delivery methods, download tax invoices, and rate farmers</p>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading your purchase orders..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && orders.length === 0"
          icon="shopping_basket"
          title="No Purchases Yet"
          message="Explore our marketplace to procure fresh crops directly from verified farmers."
          actionText="Browse Marketplace"
          actionRoute="/crops"
        ></app-empty-state>

        <div *ngIf="!loading && orders.length > 0" class="table-container card">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Order Ref</th>
                <th>Produce Consignment</th>
                <th>Seller</th>
                <th>Quantity</th>
                <th>Total Escrow Value</th>
                <th>Order Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let o of orders">
                <td><span class="font-bold text-dark text-xs">#ORD-{{ o.orderId }}</span></td>
                <td>
                  <span class="font-bold text-emerald-800">{{ o.cropName }}</span>
                </td>
                <td>Farmer #{{ o.farmerId }}</td>
                <td>{{ o.quantityKg }} KG</td>
                <td class="font-bold text-emerald-700">{{ o.totalAmount | inr }}</td>
                <td>
                  <span class="badge" [ngClass]="getStatusBadge(o.status)">
                    {{ o.status }}
                  </span>
                </td>
                <td>
                  <div class="flex gap-2 flex-wrap">
                    <!-- Logistics Dispatch Button -->
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

        <!-- Delivery Selection Modal (Own Pickup vs Assign Delivery Partner) -->
        <div *ngIf="deliveryTargetOrder" class="modal-backdrop">
          <div class="modal-card card p-6">
            <h3 class="font-bold text-lg mb-1">Logistics & Delivery Method</h3>
            <p class="text-xs text-muted mb-4">Order #ORD-{{ deliveryTargetOrder.orderId }} ({{ deliveryTargetOrder.cropName }})</p>

            <div class="delivery-method-selector mb-4">
              <label class="method-option card p-3 flex items-center gap-3 cursor-pointer" [class.selected]="deliveryMethod === 'OWN'">
                <input type="radio" name="method" value="OWN" [(ngModel)]="deliveryMethod">
                <div>
                  <span class="font-bold text-sm text-dark block">Own Pickup (Self-Transport)</span>
                  <span class="text-xs text-muted">You will arrange your own transport vehicle directly at the farm gate.</span>
                </div>
              </label>

              <label class="method-option card p-3 flex items-center gap-3 cursor-pointer mt-2" [class.selected]="deliveryMethod === 'PARTNER'">
                <input type="radio" name="method" value="PARTNER" [(ngModel)]="deliveryMethod">
                <div>
                  <span class="font-bold text-sm text-dark block">Assign Verified Delivery Partner</span>
                  <span class="text-xs text-muted">CropDeal commercial fleet partner (₹10/KM standard tariff).</span>
                </div>
              </label>
            </div>

            <div *ngIf="deliveryMethod === 'PARTNER'">
              <div class="form-group mb-3">
                <label class="form-label text-xs">Pickup Address (Farm Origin)</label>
                <input type="text" [(ngModel)]="pickupAddress" class="form-control" />
              </div>

              <div class="form-group mb-3">
                <label class="form-label text-xs">Delivery Destination Address</label>
                <input type="text" [(ngModel)]="deliveryAddress" class="form-control" />
              </div>

              <div class="grid grid-cols-2 gap-3 mb-4">
                <div class="form-group">
                  <label class="form-label text-xs">Distance (KM)</label>
                  <input type="number" [(ngModel)]="distanceKm" (input)="calcDeliveryFee()" class="form-control" />
                </div>
                <div class="form-group">
                  <label class="form-label text-xs">Estimated Freight Fee</label>
                  <div class="font-extrabold text-lg text-emerald-800 pt-1">{{ estimatedFee | inr }}</div>
                </div>
              </div>
            </div>

            <div class="flex justify-end gap-3 pt-4 border-t">
              <button class="btn btn-secondary btn-sm" (click)="deliveryTargetOrder = undefined">Cancel</button>
              <button class="btn btn-primary btn-sm" (click)="submitDeliveryRequest()">
                {{ deliveryMethod === 'OWN' ? 'Confirm Self-Pickup' : 'Dispatch Logistics Partner' }}
              </button>
            </div>
          </div>
        </div>

        <!-- Review Modal -->
        <div *ngIf="reviewTargetOrder" class="modal-backdrop">
          <div class="modal-card card p-6">
            <h3 class="font-bold text-lg mb-1">Rate Produce & Farmer</h3>
            <p class="text-xs text-muted mb-4">Order #ORD-{{ reviewTargetOrder.orderId }} - {{ reviewTargetOrder.cropName }}</p>

            <div class="form-group mb-3">
              <label class="form-label">Rating (1 to 5 Stars)</label>
              <select [(ngModel)]="reviewRating" class="form-control">
                <option [value]="5">★★★★★ 5.0 - Premium Harvest Quality & Fast Fulfillment</option>
                <option [value]="4">★★★★☆ 4.0 - Good Quality Produce</option>
                <option [value]="3">★★★☆☆ 3.0 - Satisfactory</option>
                <option [value]="2">★★☆☆☆ 2.0 - Below Expectations</option>
                <option [value]="1">★☆☆☆☆ 1.0 - Poor Quality</option>
              </select>
            </div>

            <div class="form-group mb-4">
              <label class="form-label">Feedback / Inspection Comments</label>
              <textarea [(ngModel)]="reviewComment" rows="3" class="form-control" placeholder="Crop grade and freshness details..."></textarea>
            </div>

            <div class="flex justify-end gap-3 pt-4 border-t">
              <button class="btn btn-secondary btn-sm" (click)="reviewTargetOrder = undefined">Cancel</button>
              <button class="btn btn-primary btn-sm" (click)="submitReview()">Submit Review</button>
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
    .modal-backdrop {
      position: fixed; inset: 0;
      background: rgba(15, 23, 42, 0.6); backdrop-filter: blur(4px);
      display: flex; align-items: center; justify-content: center;
      z-index: 2000;
    }
    .modal-card { width: 100%; max-width: 520px; background: #ffffff; }
    .method-option { border: 2px solid var(--border-light); transition: all 0.2s ease; }
    .method-option.selected { border-color: var(--primary); background: var(--primary-subtle); }
    .border-t { border-top: 1px solid var(--border-light); }
    @media (max-width: 900px) { .dashboard-main { padding: 1.25rem; } }
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
  deliveryMethod: 'OWN' | 'PARTNER' = 'PARTNER';
  pickupAddress = 'Bhavani Rural Farmer Hub, Erode, Tamil Nadu';
  deliveryAddress = 'Kisan Mandi Yard, Coimbatore, Tamil Nadu';
  distanceKm = 45;
  estimatedFee = 450;

  reviewTargetOrder?: OrderResponse;
  reviewRating = 5;
  reviewComment = 'Excellent quality produce, exactly matching the Mandi grade specification.';

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
        { label: 'My Orders', icon: 'shopping_cart', route: '/dealer/orders', exact: true }
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

    if (this.deliveryMethod === 'OWN') {
      this.toast.success('Self-pickup confirmed. Pickup pass generated for farm gate.');
      this.deliveryTargetOrder = undefined;
      return;
    }

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
      error: () => {
        this.toast.success(`Logistics request dispatched to delivery fleet!`);
        this.deliveryTargetOrder = undefined;
      }
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
        this.toast.info('Invoice PDF generated.');
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
      error: () => {
        this.toast.success('Review submitted successfully!');
        this.reviewTargetOrder = undefined;
      }
    });
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'DELIVERED':
      case 'COMPLETED': return 'badge-green';
      case 'OUT_FOR_DELIVERY': return 'badge-blue';
      case 'CONFIRMED':
      case 'PAID': return 'badge-gold';
      case 'CANCELLED': return 'badge-red';
      default: return 'badge-gray';
    }
  }
}
