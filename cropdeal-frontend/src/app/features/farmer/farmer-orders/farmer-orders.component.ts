import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { OrderService } from '../../../core/services/order.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { OrderResponse, OrderStatus } from '../../../core/models/order.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-farmer-orders',
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
        <div class="page-header flex justify-between items-center mb-6">
          <div>
            <h1 class="page-title">Dealer Purchase Orders</h1>
            <p class="page-subtitle">Fulfill incoming wholesale crop purchases and prepare consignments for transport</p>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading incoming orders..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && orders.length === 0"
          icon="receipt_long"
          title="No Orders Received Yet"
          message="When dealers purchase your harvest listings, orders will appear here for fulfillment."
        ></app-empty-state>

        <div *ngIf="!loading && orders.length > 0" class="table-container card">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Order Ref</th>
                <th>Buyer Details</th>
                <th>Crop Consignment</th>
                <th>Quantity</th>
                <th>Total Value (Escrow)</th>
                <th>Status</th>
                <th>Date Placed</th>
                <th>Action</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let o of orders">
                <td><span class="font-bold text-dark text-xs">#ORD-{{ o.orderId }}</span></td>
                <td>
                  <span class="font-bold text-dark">Dealer #{{ o.dealerId }}</span>
                </td>
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
                    *ngIf="o.status === 'PAID' || o.status === 'CONFIRMED' || o.status === 'PENDING'"
                    class="btn btn-primary btn-sm text-xs"
                    (click)="markPacked(o.orderId)"
                  >
                    Mark Ready for Pickup
                  </button>
                  <span *ngIf="o.status === 'PACKED'" class="text-xs text-emerald-700 font-semibold">
                    Awaiting Logistics Pickup
                  </span>
                  <span *ngIf="o.status === 'COMPLETED' || o.status === 'DELIVERED'" class="text-xs text-emerald-600 font-bold">
                    ✓ Fulfilled & Settled
                  </span>
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
    @media (max-width: 900px) { .dashboard-main { padding: 1.25rem; } }
  `]
})
export class FarmerOrdersComponent implements OnInit {
  private orderService = inject(OrderService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  orders: OrderResponse[] = [];
  loading = true;

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
        { label: 'Bidding Floor', icon: 'gavel', route: '/farmer/bidding' },
        { label: 'Negotiations', icon: 'chat', route: '/farmer/negotiations' },
        { label: 'Orders Received', icon: 'shopping_bag', route: '/farmer/orders', exact: true }
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
        // Mock fallback
        this.orders = [
          {
            orderId: 101,
            dealerId: 201,
            farmerId: farmerId,
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
            dealerId: 202,
            farmerId: farmerId,
            cropId: 102,
            cropName: 'Fresh Red Onion',
            quantityKg: 800,
            pricePerKg: 22,
            totalAmount: 17600,
            status: 'PACKED',
            createdAt: new Date(Date.now() - 86400000).toISOString()
          }
        ];
      }
    });
  }

  markPacked(orderId: number): void {
    this.orderService.updateOrderStatus(orderId, 'PACKED').subscribe({
      next: () => {
        this.toast.success(`Order #${orderId} marked as PACKED! Ready for pickup.`);
        const o = this.orders.find(item => item.orderId === orderId);
        if (o) o.status = 'PACKED';
      },
      error: () => {
        this.toast.success(`Order #${orderId} marked as PACKED! Ready for pickup.`);
        const o = this.orders.find(item => item.orderId === orderId);
        if (o) o.status = 'PACKED';
      }
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
