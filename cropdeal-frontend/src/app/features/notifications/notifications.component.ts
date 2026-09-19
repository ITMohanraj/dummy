import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { NotificationRecord } from '../../core/models/notification.models';
import { SidebarComponent, NavSection } from '../../shared/components/sidebar/sidebar.component';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, LoadingSpinnerComponent, EmptyStateComponent],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <div class="page-header flex justify-between items-center mb-6">
          <div>
            <h1 class="page-title">Notification Alerts</h1>
            <p class="page-subtitle">Real-time alerts for orders, payments, live auction bids, and APMC price trends</p>
          </div>

          <button class="btn btn-secondary btn-sm" (click)="markAllAsRead()">
            <span class="material-symbols-outlined text-sm">done_all</span>
            Mark All as Read
          </button>
        </div>

        <!-- Filter Badges -->
        <div class="filter-bar flex gap-2 mb-6">
          <button class="filter-btn" [class.active]="currentFilter === 'ALL'" (click)="currentFilter = 'ALL'">
            All Alerts ({{ notifications.length }})
          </button>
          <button class="filter-btn" [class.active]="currentFilter === 'ORDERS'" (click)="currentFilter = 'ORDERS'">
            Orders
          </button>
          <button class="filter-btn" [class.active]="currentFilter === 'BIDS'" (click)="currentFilter = 'BIDS'">
            Auction Bids
          </button>
          <button class="filter-btn" [class.active]="currentFilter === 'PAYMENTS'" (click)="currentFilter = 'PAYMENTS'">
            Payments & Escrow
          </button>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading your notifications..."></app-loading-spinner>

        <div *ngIf="!loading && filteredNotifications.length > 0" class="notifications-list flex flex-col gap-3">
          <div 
            *ngFor="let n of filteredNotifications" 
            class="notif-card card p-4 flex items-start gap-4"
            [class.unread]="!n.isRead"
          >
            <div class="notif-icon-circle" [ngClass]="getIconColor(n.type)">
              <span class="material-symbols-outlined">{{ getIcon(n.type) }}</span>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-center mb-1">
                <h3 class="font-bold text-sm text-dark">{{ n.title }}</h3>
                <span class="text-xs text-muted">{{ n.createdAt | date:'medium' }}</span>
              </div>
              <p class="text-xs text-slate-700">{{ n.message }}</p>
            </div>
          </div>
        </div>

        <app-empty-state 
          *ngIf="!loading && filteredNotifications.length === 0"
          icon="notifications_off"
          title="No Notifications in this Category"
          message="You are completely caught up! New order, bid, and escrow alerts will show here."
        ></app-empty-state>
      </main>
    </div>
  `,
  styles: [`
    .dashboard-layout { display: flex; min-height: calc(100vh - 120px); background: var(--bg-main); }
    .dashboard-main { flex: 1; padding: 2rem 2.5rem; max-width: 1100px; }
    .page-header { margin-bottom: 1.5rem; }
    .page-title { font-size: 1.625rem; font-weight: 800; color: var(--dark); }
    .page-subtitle { font-size: 0.875rem; color: var(--text-muted); margin-top: 0.25rem; }
    .filter-btn {
      padding: 0.5rem 1rem; border-radius: var(--radius-full);
      background: #ffffff; border: 1px solid var(--border-light);
      font-size: 0.8125rem; font-weight: 600; color: var(--text-muted); cursor: pointer;
    }
    .filter-btn.active { background: var(--primary); color: #ffffff; border-color: var(--primary); }
    .notif-card {
      transition: all 0.2s ease;
    }
    .notif-card.unread {
      background: #f0fdf4;
      border-left: 4px solid var(--primary);
    }
    .notif-icon-circle {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .notif-icon-circle.bg-blue { background: #e0f2fe; color: #0284c7; }
    .notif-icon-circle.bg-amber { background: #fef3c7; color: #d97706; }
    .notif-icon-circle.bg-purple { background: #f3e8ff; color: #7e22ce; }
    @media (max-width: 900px) { .dashboard-main { padding: 1.25rem; } }
  `]
})
export class NotificationsComponent implements OnInit {
  private notificationService = inject(NotificationService);
  authService = inject(AuthService);
  private toast = inject(ToastService);

  notifications: NotificationRecord[] = [];
  loading = true;
  currentFilter: 'ALL' | 'ORDERS' | 'BIDS' | 'PAYMENTS' = 'ALL';

  sidebarSections: NavSection[] = [];

  get filteredNotifications(): NotificationRecord[] {
    if (this.currentFilter === 'ALL') return this.notifications;
    if (this.currentFilter === 'ORDERS') return this.notifications.filter(n => n.type.includes('ORDER'));
    if (this.currentFilter === 'BIDS') return this.notifications.filter(n => n.type.includes('BID'));
    if (this.currentFilter === 'PAYMENTS') return this.notifications.filter(n => n.type.includes('PAYMENT') || n.type.includes('ESCROW'));
    return this.notifications;
  }

  ngOnInit(): void {
    this.setupSidebar();
    const userId = this.authService.getUserId() || 101;
    this.loading = true;
    this.notificationService.getUserNotifications(userId).subscribe({
      next: (res) => {
        this.notifications = res || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        // Mock fallback
        this.notifications = [
          {
            id: 1,
            userId: userId,
            title: 'Order Paid & Confirmed',
            message: 'Dealer Kisan Mandi Traders has paid ₹44,000 in escrow for 500 KG Erode Turmeric.',
            type: 'ORDER_PLACED',
            isRead: false,
            createdAt: new Date().toISOString()
          },
          {
            id: 2,
            userId: userId,
            title: 'New Bid Received',
            message: 'Apex Food Impex placed a new bid of ₹38.50/KG for your Paddy harvest.',
            type: 'PRICE_ALERT',
            isRead: true,
            createdAt: new Date(Date.now() - 3600000).toISOString()
          }
        ];
      }
    });
  }

  setupSidebar(): void {
    const role = this.authService.getUserRole() || 'FARMER';
    if (role === 'FARMER') {
      this.sidebarSections = [
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
            { label: 'Notifications', icon: 'notifications', route: '/notifications', exact: true }
          ]
        }
      ];
    } else if (role === 'DEALER') {
      this.sidebarSections = [
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
            { label: 'My Orders', icon: 'shopping_cart', route: '/dealer/orders' }
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
            { label: 'Notifications', icon: 'notifications', route: '/notifications', exact: true }
          ]
        }
      ];
    } else {
      this.sidebarSections = [
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
            { label: 'Notifications', icon: 'notifications', route: '/notifications', exact: true }
          ]
        }
      ];
    }
  }

  markAllAsRead(): void {
    const userId = this.authService.getUserId() || 101;
    this.notificationService.markAllAsRead(userId).subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
        this.toast.success('All notifications marked as read.');
      },
      error: () => {
        this.notifications.forEach(n => n.isRead = true);
      }
    });
  }

  getIcon(type: string): string {
    if (type.includes('ORDER')) return 'shopping_bag';
    if (type.includes('PAYMENT') || type.includes('ESCROW')) return 'payments';
    if (type.includes('DELIVERY')) return 'local_shipping';
    if (type.includes('BID')) return 'gavel';
    return 'notifications';
  }

  getIconColor(type: string): string {
    if (type.includes('ORDER')) return 'bg-blue';
    if (type.includes('BID')) return 'bg-purple';
    if (type.includes('PAYMENT')) return 'bg-amber';
    return '';
  }
}
