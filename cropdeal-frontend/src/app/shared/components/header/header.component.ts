import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationRecord } from '../../../core/models/notification.models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header class="navbar">
      <!-- Top Announcement / Live Mandi Ticker Bar -->
      <div class="top-bar">
        <div class="container flex justify-between items-center text-xs">
          <div class="flex items-center gap-2">
            <span class="badge badge-green text-2xs">Live APMC Sync</span>
            <span class="text-emerald-100">1,000+ daily Mandi rates synchronized from official data.gov.in</span>
          </div>
          <div class="flex items-center gap-4">
            <span class="text-emerald-200">Kisan Helpline: 1800-419-CROP</span>
            <span class="text-emerald-200 hide-mobile">Support: 24/7 Agri Desk</span>
          </div>
        </div>
      </div>

      <!-- Main Navigation -->
      <div class="main-nav">
        <div class="container flex justify-between items-center">
          
          <!-- CropDeal Logo -->
          <a routerLink="/" class="logo flex items-center gap-2">
            <div class="logo-icon">
              <span class="material-symbols-outlined">eco</span>
            </div>
            <div class="logo-text">
              <span class="brand-title">Crop<span class="brand-highlight">Deal</span></span>
              <span class="brand-sub">Direct Agri Marketplace</span>
            </div>
          </a>

          <!-- Navigation Links -->
          <nav class="nav-links flex items-center gap-6">
            <a routerLink="/" routerLinkActive="active" [routerLinkActiveOptions]="{exact: true}">Home</a>
            <a routerLink="/crops" routerLinkActive="active">Crops</a>
            <a routerLink="/market-prices" routerLinkActive="active">
              <span class="flex items-center gap-1">
                Market Prices
                <span class="badge badge-gold badge-xs">Live</span>
              </span>
            </a>
            <a routerLink="/auctions" routerLinkActive="active">Auctions</a>
            <a routerLink="/assistant" routerLinkActive="active" class="flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">smart_toy</span>
              AI Advisor
            </a>

            <!-- Direct Role Dashboard Link when Logged In -->
            <ng-container *ngIf="authService.isAuthenticated()">
              <a 
                [routerLink]="authService.getRoleDashboardUrl()" 
                routerLinkActive="active" 
                class="portal-link"
              >
                <span class="material-symbols-outlined text-sm">dashboard</span>
                Dashboard
              </a>
            </ng-container>
          </nav>

          <!-- Right Action Bar -->
          <div class="nav-actions flex items-center gap-3">
            
            <!-- Guest Links -->
            <ng-container *ngIf="!authService.isAuthenticated()">
              <a routerLink="/login" class="btn btn-secondary btn-sm">Login</a>
              <a routerLink="/register" class="btn btn-primary btn-sm">Register</a>
            </ng-container>

            <!-- Authenticated User Menu -->
            <ng-container *ngIf="authService.isAuthenticated()">
              <!-- Notifications Bell -->
              <div class="notification-wrapper">
                <button class="icon-btn" (click)="toggleNotifications()" title="Notifications">
                  <span class="material-symbols-outlined">notifications</span>
                  <span *ngIf="unreadCount > 0" class="notif-badge">{{ unreadCount }}</span>
                </button>

                <!-- Notifications Dropdown -->
                <div *ngIf="showNotifications" class="notif-dropdown card">
                  <div class="notif-header flex justify-between items-center">
                    <span class="font-bold text-sm">Notifications</span>
                    <button class="text-xs text-emerald-600 font-semibold" (click)="markAllRead()">Mark all read</button>
                  </div>
                  <div class="notif-list">
                    <div *ngIf="notifications.length === 0" class="p-4 text-center text-xs text-muted">
                      No notifications
                    </div>
                    <div 
                      *ngFor="let n of notifications.slice(0, 5)" 
                      class="notif-item"
                      [class.unread]="!n.isRead"
                      (click)="markRead(n.id)"
                    >
                      <div class="notif-title">{{ n.title }}</div>
                      <div class="notif-msg">{{ n.message }}</div>
                      <div class="notif-time">{{ n.createdAt | date:'shortTime' }}</div>
                    </div>
                  </div>
                  <div class="notif-footer">
                    <a routerLink="/notifications" (click)="showNotifications = false" class="text-xs text-center block text-emerald-700 font-bold">View all notifications</a>
                  </div>
                </div>
              </div>

              <!-- Wallet Quick Access -->
              <a routerLink="/wallet" class="icon-btn" title="Wallet">
                <span class="material-symbols-outlined">account_balance_wallet</span>
              </a>

              <!-- User Profile Link -->
              <a routerLink="/profile" class="user-badge flex items-center gap-2" title="My Profile">
                <div class="avatar">{{ getUserInitials() }}</div>
                <div class="user-info hide-mobile">
                  <div class="user-name">{{ authService.currentUser()?.fullName }}</div>
                  <div class="user-role badge badge-green badge-xs">{{ authService.getUserRole() }}</div>
                </div>
              </a>

              <!-- Logout Button -->
              <button class="icon-btn logout-icon" (click)="logout()" title="Logout">
                <span class="material-symbols-outlined">logout</span>
              </button>
            </ng-container>

            <!-- Sell Crop CTA Button -->
            <a 
              *ngIf="authService.getUserRole() === 'FARMER' || !authService.isAuthenticated()" 
              [routerLink]="authService.isAuthenticated() ? '/farmer/crops/new' : '/login'" 
              class="btn btn-accent btn-sm hide-mobile"
            >
              <span class="material-symbols-outlined text-sm">add_circle</span>
              Sell Crop
            </a>
          </div>
        </div>
      </div>
    </header>
  `,
  styles: [`
    .navbar {
      background: #ffffff;
      border-bottom: 1px solid var(--border-light);
      position: sticky;
      top: 0;
      z-index: 1000;
      box-shadow: var(--shadow-sm);
    }
    .top-bar {
      background: #14532d;
      color: #ffffff;
      padding: 0.35rem 0;
      font-size: 0.75rem;
    }
    .main-nav {
      padding: 0.75rem 0;
    }
    .logo {
      display: flex;
      align-items: center;
      gap: 0.75rem;
    }
    .logo-icon {
      width: 40px;
      height: 40px;
      border-radius: var(--radius-md);
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      box-shadow: 0 2px 6px rgba(21, 128, 61, 0.3);
    }
    .logo-icon span { font-size: 26px; }
    .brand-title {
      font-family: var(--font-heading);
      font-size: 1.35rem;
      font-weight: 800;
      color: var(--dark);
      letter-spacing: -0.02em;
    }
    .brand-highlight {
      color: var(--primary);
    }
    .brand-sub {
      display: block;
      font-size: 0.6875rem;
      font-weight: 600;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.05em;
      margin-top: -3px;
    }
    .nav-links a {
      font-size: 0.9375rem;
      font-weight: 600;
      color: var(--text-main);
      padding: 0.375rem 0.5rem;
      border-radius: var(--radius-sm);
    }
    .nav-links a:hover, .nav-links a.active {
      color: var(--primary);
    }
    .portal-link {
      display: inline-flex;
      align-items: center;
      gap: 0.35rem;
      background: var(--primary-subtle);
      color: var(--primary) !important;
      border: 1px solid var(--primary-border);
      padding: 0.25rem 0.75rem !important;
      border-radius: var(--radius-full) !important;
      font-size: 0.8125rem !important;
      font-weight: 700 !important;
    }
    .icon-btn {
      position: relative;
      background: #f8fafc;
      border: 1px solid var(--border-light);
      color: var(--text-main);
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 50%;
      transition: all 0.2s ease;
    }
    .icon-btn:hover { background: var(--primary-subtle); color: var(--primary); border-color: var(--primary-border); }
    .logout-icon:hover { background: #fee2e2; color: var(--danger); border-color: #fca5a5; }
    .notif-badge {
      position: absolute;
      top: -3px;
      right: -3px;
      background: var(--danger);
      color: #ffffff;
      font-size: 0.625rem;
      font-weight: 700;
      width: 17px;
      height: 17px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .notification-wrapper { position: relative; }
    .notif-dropdown {
      position: absolute;
      right: 0;
      top: 100%;
      width: 320px;
      margin-top: 0.5rem;
      z-index: 100;
      box-shadow: var(--shadow-xl);
    }
    .notif-header { padding: 0.75rem 1rem; border-bottom: 1px solid var(--border-light); background: #f8fafc; }
    .notif-item { padding: 0.75rem 1rem; border-bottom: 1px solid var(--border-light); cursor: pointer; }
    .notif-item:hover { background: var(--primary-subtle); }
    .notif-item.unread { background: #f0fdf4; border-left: 3px solid var(--primary); }
    .notif-title { font-size: 0.8125rem; font-weight: 700; }
    .notif-msg { font-size: 0.75rem; color: var(--text-muted); margin: 2px 0; }
    .notif-time { font-size: 0.6875rem; color: var(--text-light); }
    .notif-footer { padding: 0.5rem; background: #f8fafc; }
    .user-badge {
      padding: 0.25rem 0.5rem;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-full);
      background: #f8fafc;
      transition: all 0.2s ease;
    }
    .user-badge:hover {
      background: var(--primary-subtle);
      border-color: var(--primary-border);
    }
    .avatar {
      width: 30px;
      height: 30px;
      border-radius: 50%;
      background: var(--primary);
      color: #ffffff;
      font-size: 0.75rem;
      font-weight: 700;
      display: flex;
      align-items: center;
      justify-content: center;
    }
    .user-name { font-size: 0.8125rem; font-weight: 700; line-height: 1.1; max-width: 120px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .badge-xs { font-size: 0.625rem; padding: 1px 5px; }
    .text-2xs { font-size: 0.625rem; }
    @media (max-width: 1024px) {
      .hide-mobile { display: none; }
    }
  `]
})
export class HeaderComponent implements OnInit, OnDestroy {
  authService = inject(AuthService);
  notificationService = inject(NotificationService);
  router = inject(Router);

  unreadCount = 0;
  notifications: NotificationRecord[] = [];
  showNotifications = false;
  private pollSub?: Subscription;

  ngOnInit(): void {
    if (this.authService.isAuthenticated()) {
      this.loadNotifications();
      this.pollSub = interval(25000).subscribe(() => this.loadNotifications());
    }
  }

  ngOnDestroy(): void {
    this.pollSub?.unsubscribe();
  }

  loadNotifications(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.notificationService.getUserNotifications(userId).subscribe({
      next: (res) => {
        this.notifications = res;
        this.unreadCount = res.filter(n => !n.isRead).length;
      },
      error: () => {}
    });
  }

  toggleNotifications(): void {
    this.showNotifications = !this.showNotifications;
  }

  markRead(id: number): void {
    this.notificationService.markAsRead(id).subscribe(() => {
      const item = this.notifications.find(n => n.id === id);
      if (item) item.isRead = true;
      this.unreadCount = Math.max(0, this.unreadCount - 1);
    });
  }

  markAllRead(): void {
    const userId = this.authService.getUserId();
    if (!userId) return;

    this.notificationService.markAllAsRead(userId).subscribe(() => {
      this.notifications.forEach(n => n.isRead = true);
      this.unreadCount = 0;
    });
  }

  getUserInitials(): string {
    const name = this.authService.currentUser()?.fullName || 'User';
    return name.split(' ').map(p => p[0]).join('').substring(0, 2).toUpperCase();
  }

  logout(): void {
    this.authService.logout();
  }
}
