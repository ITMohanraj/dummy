import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service';
import { NotificationRecord } from '../../../core/models/notification.models';
import { Role } from '../../../core/models/auth.models';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <header class="navbar">
      <!-- Top Announcement / Mandi Ticker Bar -->
      <div class="top-bar">
        <div class="container flex justify-between items-center text-xs">
          <div class="flex items-center gap-2">
            <span class="badge badge-green text-2xs">Live APMC Sync</span>
            <span class="text-emerald-100">1,000+ daily Mandi rates synchronized from official data.gov.in</span>
          </div>
          <div class="flex items-center gap-4">
            <span class="text-emerald-200">Helpline: 1800-419-CROP</span>
            <!-- Quick Role Demo Switcher -->
            <div class="role-switcher" *ngIf="authService.isAuthenticated()">
              <span class="switcher-label">Role:</span>
              <button 
                class="role-btn" 
                [class.active]="authService.getUserRole() === 'FARMER'"
                (click)="switchRole('FARMER', 'Ramesh Kumar (Farmer)', 101)"
              >Farmer</button>
              <button 
                class="role-btn" 
                [class.active]="authService.getUserRole() === 'DEALER'"
                (click)="switchRole('DEALER', 'Kisan Mandi Traders', 201)"
              >Dealer</button>
              <button 
                class="role-btn" 
                [class.active]="authService.getUserRole() === 'DELIVERY_PARTNER'"
                (click)="switchRole('DELIVERY_PARTNER', 'AgriLogistics Express', 301)"
              >Delivery</button>
              <button 
                class="role-btn" 
                [class.active]="authService.getUserRole() === 'ADMIN'"
                (click)="switchRole('ADMIN', 'CropDeal Administrator', 999)"
              >Admin</button>
            </div>
          </div>
        </div>
      </div>

      <!-- Main Navigation -->
      <div class="main-nav">
        <div class="container flex justify-between items-center">
          
          <!-- Logo -->
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
            <a routerLink="/crops" routerLinkActive="active">Marketplace</a>
            <a routerLink="/market-prices" routerLinkActive="active">
              <span class="flex items-center gap-1">
                Mandi Rates
                <span class="badge badge-gold badge-xs">Live</span>
              </span>
            </a>
            <a routerLink="/auctions" routerLinkActive="active">Auctions</a>
            <a routerLink="/assistant" routerLinkActive="active" class="flex items-center gap-1">
              <span class="material-symbols-outlined text-sm">smart_toy</span>
              AI Advisor
            </a>

            <!-- Role-Specific Portal Links -->
            <ng-container *ngIf="authService.isAuthenticated()">
              <a *ngIf="authService.getUserRole() === 'FARMER'" routerLink="/farmer/dashboard" routerLinkActive="active" class="portal-link">Farmer Portal</a>
              <a *ngIf="authService.getUserRole() === 'DEALER'" routerLink="/dealer/dashboard" routerLinkActive="active" class="portal-link">Dealer Portal</a>
              <a *ngIf="authService.getUserRole() === 'DELIVERY_PARTNER'" routerLink="/delivery/dashboard" routerLinkActive="active" class="portal-link">Delivery Hub</a>
              <a *ngIf="authService.getUserRole() === 'ADMIN'" routerLink="/admin/dashboard" routerLinkActive="active" class="portal-link">Admin Console</a>
            </ng-container>
          </nav>

          <!-- Right Action Bar -->
          <div class="nav-actions flex items-center gap-4">
            
            <ng-container *ngIf="!authService.isAuthenticated()">
              <a routerLink="/login" class="btn btn-secondary btn-sm">Login</a>
              <a routerLink="/register" class="btn btn-primary btn-sm">Register</a>
            </ng-container>

            <ng-container *ngIf="authService.isAuthenticated()">
              <!-- Notifications Bell -->
              <div class="notification-wrapper">
                <button class="icon-btn" (click)="toggleNotifications()">
                  <span class="material-symbols-outlined">notifications</span>
                  <span *ngIf="unreadCount > 0" class="notif-badge">{{ unreadCount }}</span>
                </button>

                <!-- Notifications Dropdown -->
                <div *ngIf="showNotifications" class="notif-dropdown card">
                  <div class="notif-header flex justify-between items-center">
                    <span class="font-bold text-sm">Notifications</span>
                    <button class="text-xs text-emerald-600 font-semibold" (click)="markAllRead()">Mark all as read</button>
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

              <!-- User Menu Dropdown -->
              <div class="user-badge flex items-center gap-2">
                <div class="avatar">{{ getUserInitials() }}</div>
                <div class="user-info">
                  <div class="user-name">{{ authService.currentUser()?.fullName }}</div>
                  <div class="user-role badge badge-green badge-xs">{{ authService.getUserRole() }}</div>
                </div>
                <button class="btn btn-secondary btn-sm logout-btn" (click)="logout()" title="Logout">
                  <span class="material-symbols-outlined text-sm">logout</span>
                </button>
              </div>
            </ng-container>

            <!-- Sell Crop CTA Button -->
            <a 
              *ngIf="authService.getUserRole() === 'FARMER' || !authService.isAuthenticated()" 
              routerLink="/farmer/crops/new" 
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
      background: var(--primary-subtle);
      color: var(--primary) !important;
      border: 1px solid var(--primary-border);
      padding: 0.25rem 0.625rem !important;
      border-radius: var(--radius-full) !important;
      font-size: 0.8125rem !important;
    }
    .role-switcher {
      display: inline-flex;
      align-items: center;
      gap: 0.25rem;
      background: rgba(0, 0, 0, 0.2);
      padding: 2px 6px;
      border-radius: var(--radius-full);
    }
    .switcher-label { color: #86efac; font-weight: 700; margin-right: 2px; }
    .role-btn {
      background: transparent;
      border: none;
      color: #e2e8f0;
      font-size: 0.6875rem;
      font-weight: 600;
      padding: 1px 6px;
      border-radius: var(--radius-full);
      cursor: pointer;
    }
    .role-btn.active {
      background: #22c55e;
      color: #052e16;
      font-weight: 700;
    }
    .icon-btn {
      position: relative;
      background: none;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      display: flex;
      align-items: center;
      padding: 0.375rem;
      border-radius: 50%;
    }
    .icon-btn:hover { background: var(--bg-main); color: var(--dark); }
    .notif-badge {
      position: absolute;
      top: -2px;
      right: -2px;
      background: var(--danger);
      color: #ffffff;
      font-size: 0.625rem;
      font-weight: 700;
      width: 16px;
      height: 16px;
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
      this.pollSub = interval(20000).subscribe(() => this.loadNotifications());
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

  switchRole(role: Role, name: string, id: number): void {
    this.authService.switchRoleSession(role, name, id);
    if (role === 'FARMER') this.router.navigate(['/farmer/dashboard']);
    else if (role === 'DEALER') this.router.navigate(['/dealer/dashboard']);
    else if (role === 'DELIVERY_PARTNER') this.router.navigate(['/delivery/dashboard']);
    else if (role === 'ADMIN') this.router.navigate(['/admin/dashboard']);
  }

  getUserInitials(): string {
    const name = this.authService.currentUser()?.fullName || 'User';
    return name.split(' ').map(p => p[0]).join('').substring(0, 2).toUpperCase();
  }

  logout(): void {
    this.authService.logout();
  }
}
