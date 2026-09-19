import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { NotificationRecord } from '../../core/models/notification.models';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-notifications',
  standalone: true,
  imports: [CommonModule, RouterModule, LoadingSpinnerComponent, EmptyStateComponent],
  template: `
    <div class="notifications-page py-8">
      <div class="container max-w-3xl">
        
        <div class="flex justify-between items-center mb-6">
          <div>
            <h1 class="text-2xl font-extrabold text-dark">Notification Alerts</h1>
            <p class="text-xs text-muted">Real-time alerts for orders, payments, logistics, and price movements.</p>
          </div>

          <button class="btn btn-secondary btn-sm" (click)="markAllAsRead()">
            Mark All as Read
          </button>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading your notifications..."></app-loading-spinner>

        <div *ngIf="!loading && notifications.length > 0" class="notifications-list flex flex-col gap-3">
          <div 
            *ngFor="let n of notifications" 
            class="notif-card card p-4 flex items-start gap-3"
            [class.unread]="!n.isRead"
          >
            <div class="notif-icon-circle">
              <span class="material-symbols-outlined text-sm">{{ getIcon(n.type) }}</span>
            </div>
            <div class="flex-1">
              <div class="flex justify-between items-center mb-1">
                <h3 class="font-bold text-sm text-dark">{{ n.title }}</h3>
                <span class="text-2xs text-muted">{{ n.createdAt | date:'medium' }}</span>
              </div>
              <p class="text-xs text-slate-600">{{ n.message }}</p>
            </div>
          </div>
        </div>

        <app-empty-state 
          *ngIf="!loading && notifications.length === 0"
          icon="notifications_off"
          title="No Notifications"
          description="You are completely caught up! New order and payment alerts will show here."
        ></app-empty-state>

      </div>
    </div>
  `,
  styles: [`
    .notif-card.unread {
      background: #f0fdf4;
      border-left: 4px solid var(--primary);
    }
    .notif-icon-circle {
      width: 36px;
      height: 36px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }
    .text-2xs { font-size: 0.625rem; }
  `]
})
export class NotificationsComponent implements OnInit {
  private notificationService = inject(NotificationService);
  private authService = inject(AuthService);

  notifications: NotificationRecord[] = [];
  loading = true;

  ngOnInit(): void {
    const userId = this.authService.getUserId() || 101;
    this.loading = true;
    this.notificationService.getUserNotifications(userId).subscribe({
      next: (res) => {
        this.notifications = res || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  markAllAsRead(): void {
    const userId = this.authService.getUserId() || 101;
    this.notificationService.markAllAsRead(userId).subscribe({
      next: () => {
        this.notifications.forEach(n => n.isRead = true);
      },
      error: () => {}
    });
  }

  getIcon(type: string): string {
    switch (type) {
      case 'ORDER_PLACED': return 'shopping_bag';
      case 'PAYMENT_RECEIVED': return 'payments';
      case 'DELIVERY_ACCEPTED': return 'local_shipping';
      case 'ESCROW_RELEASED': return 'verified_user';
      case 'PRICE_ALERT': return 'trending_down';
      default: return 'notifications';
    }
  }
}
