import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { OrderResponse, OrderStatus } from '../../../core/models/order.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-farmer-orders',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    LoadingSpinnerComponent, 
    EmptyStateComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="farmer-orders-page py-8">
      <div class="container">
        
        <div class="flex justify-between items-center mb-6">
          <div>
            <h1 class="text-2xl font-extrabold text-dark">Dealer Purchase Orders</h1>
            <p class="text-xs text-muted">Fulfill incoming crop purchases and prepare consignments for transport.</p>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading incoming orders..."></app-loading-spinner>

        <div *ngIf="!loading && orders.length > 0" class="table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Order ID</th>
                <th>Dealer</th>
                <th>Crop Consignment</th>
                <th>Quantity</th>
                <th>Total Value (Escrow)</th>
                <th>Status</th>
                <th>Order Date</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let o of orders">
                <td><span class="font-bold text-dark">#{{ o.orderId }}</span></td>
                <td>Dealer #{{ o.dealerId }}</td>
                <td>
                  <span class="font-bold text-emerald-800">{{ o.cropName }}</span>
                </td>
                <td>{{ o.quantityKg }} KG</td>
                <td>
                  <span class="font-bold text-emerald-700">{{ o.totalAmount | inr }}</span>
                </td>
                <td>
                  <span class="badge" [ngClass]="getStatusBadge(o.status)">{{ o.status }}</span>
                </td>
                <td class="text-xs text-muted">{{ o.createdAt | date:'mediumDate' }}</td>
                <td>
                  <button 
                    *ngIf="o.status === 'PAID' || o.status === 'CONFIRMED'"
                    class="btn btn-primary btn-sm text-xs"
                    (click)="markPacked(o.orderId)"
                  >
                    Mark Packed & Ready
                  </button>
                  <span *ngIf="o.status === 'PACKED'" class="text-xs text-emerald-700 font-semibold">
                    Awaiting Pickup
                  </span>
                  <span *ngIf="o.status === 'COMPLETED' || o.status === 'DELIVERED'" class="text-xs text-muted">
                    Fulfilled
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <app-empty-state 
          *ngIf="!loading && orders.length === 0"
          icon="receipt_long"
          title="No Orders Received Yet"
          description="When dealers purchase your harvest listings, orders will appear here for fulfillment."
        ></app-empty-state>

      </div>
    </div>
  `
})
export class FarmerOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  orders: OrderResponse[] = [];
  loading = true;

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders(): void {
    const farmerId = this.authService.getUserId() || 101;
    this.loading = true;
    this.orderService.getOrdersByFarmer(farmerId).subscribe({
      next: (res) => {
        this.orders = res || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  markPacked(orderId: number): void {
    this.orderService.updateOrderStatus(orderId, 'PACKED').subscribe({
      next: () => {
        this.toast.success(`Order #${orderId} marked as PACKED! Ready for delivery partner pickup.`);
        this.loadOrders();
      },
      error: () => {}
    });
  }

  getStatusBadge(status: OrderStatus): string {
    switch (status) {
      case 'PAID':
      case 'CONFIRMED': return 'badge-green';
      case 'PACKED': return 'badge-gold';
      case 'OUT_FOR_DELIVERY': return 'badge-blue';
      case 'COMPLETED':
      case 'DELIVERED': return 'badge-green';
      case 'CANCELLED': return 'badge-red';
      default: return 'badge-gray';
    }
  }
}
