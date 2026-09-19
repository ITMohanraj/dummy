import { Component, inject, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

export interface NavItem {
  label: string;
  icon: string;
  route: string;
  badge?: string | number;
  badgeClass?: string;
  exact?: boolean;
}

export interface NavSection {
  title: string;
  items: NavItem[];
}

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <aside class="sidebar-wrapper" [class.mobile-open]="isOpen">
      <!-- User Profile Card in Sidebar -->
      <div class="sidebar-profile">
        <div class="profile-avatar">
          {{ getUserInitials() }}
        </div>
        <div class="profile-details">
          <span class="user-fullname">{{ authService.currentUser()?.fullName || 'User' }}</span>
          <span class="user-role-badge badge badge-green badge-xs">{{ authService.getUserRole() }}</span>
        </div>
      </div>

      <!-- Navigation Menu -->
      <div class="nav-sections">
        <div *ngFor="let section of sections" class="nav-section">
          <div class="section-title">{{ section.title }}</div>
          <ul class="nav-list">
            <li *ngFor="let item of section.items">
              <a 
                [routerLink]="item.route" 
                routerLinkActive="active" 
                [routerLinkActiveOptions]="{ exact: item.exact ?? false }"
                class="nav-link"
                (click)="onNavigate()"
              >
                <span class="material-symbols-outlined nav-icon">{{ item.icon }}</span>
                <span class="nav-text">{{ item.label }}</span>
                <span *ngIf="item.badge" class="nav-badge" [ngClass]="item.badgeClass || 'badge-green'">{{ item.badge }}</span>
              </a>
            </li>
          </ul>
        </div>
      </div>

      <!-- Footer Quick Info -->
      <div class="sidebar-footer">
        <button class="logout-btn" (click)="logout()">
          <span class="material-symbols-outlined">logout</span>
          <span>Logout</span>
        </button>
      </div>
    </aside>
  `,
  styles: [`
    .sidebar-wrapper {
      width: 260px;
      min-width: 260px;
      background: #ffffff;
      border-right: 1px solid var(--border-light);
      min-height: calc(100vh - 120px);
      display: flex;
      flex-direction: column;
      padding: 1.5rem 1rem;
      transition: all 0.3s ease;
    }
    .sidebar-profile {
      display: flex;
      align-items: center;
      gap: 0.875rem;
      padding: 0.875rem;
      background: #f8fafc;
      border: 1px solid var(--border-light);
      border-radius: var(--radius-lg);
      margin-bottom: 1.5rem;
    }
    .profile-avatar {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      font-weight: 700;
      font-size: 0.9375rem;
      box-shadow: 0 2px 5px rgba(21, 128, 61, 0.25);
    }
    .profile-details {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
      overflow: hidden;
    }
    .user-fullname {
      font-weight: 700;
      font-size: 0.875rem;
      color: var(--dark);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
    .nav-sections {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 1.25rem;
    }
    .section-title {
      font-size: 0.6875rem;
      font-weight: 800;
      text-transform: uppercase;
      letter-spacing: 0.08em;
      color: var(--text-muted);
      margin-bottom: 0.5rem;
      padding-left: 0.75rem;
    }
    .nav-list {
      list-style: none;
      display: flex;
      flex-direction: column;
      gap: 0.25rem;
    }
    .nav-link {
      display: flex;
      align-items: center;
      gap: 0.75rem;
      padding: 0.625rem 0.875rem;
      border-radius: var(--radius-md);
      color: var(--text-main);
      font-size: 0.875rem;
      font-weight: 600;
      transition: all 0.2s ease;
    }
    .nav-link:hover {
      background: var(--primary-subtle);
      color: var(--primary);
    }
    .nav-link.active {
      background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
      color: #ffffff;
      box-shadow: 0 2px 6px rgba(21, 128, 61, 0.25);
    }
    .nav-link.active .nav-icon {
      color: #ffffff;
    }
    .nav-icon {
      font-size: 1.25rem;
      color: var(--text-muted);
      transition: color 0.2s ease;
    }
    .nav-text {
      flex: 1;
    }
    .nav-badge {
      font-size: 0.6875rem;
      padding: 2px 6px;
      border-radius: var(--radius-full);
      font-weight: 700;
    }
    .sidebar-footer {
      padding-top: 1rem;
      border-top: 1px solid var(--border-light);
      margin-top: 1rem;
    }
    .logout-btn {
      width: 100%;
      display: flex;
      align-items: center;
      gap: 0.625rem;
      padding: 0.625rem 0.875rem;
      border-radius: var(--radius-md);
      background: #fef2f2;
      border: 1px solid #fee2e2;
      color: var(--danger);
      font-size: 0.875rem;
      font-weight: 600;
      cursor: pointer;
      transition: all 0.2s ease;
    }
    .logout-btn:hover {
      background: #fee2e2;
    }

    @media (max-width: 900px) {
      .sidebar-wrapper {
        position: fixed;
        left: -280px;
        top: 60px;
        bottom: 0;
        z-index: 999;
        box-shadow: var(--shadow-xl);
      }
      .sidebar-wrapper.mobile-open {
        left: 0;
      }
    }
  `]
})
export class SidebarComponent {
  authService = inject(AuthService);
  router = inject(Router);

  @Input() sections: NavSection[] = [];
  @Input() isOpen = false;

  getUserInitials(): string {
    const name = this.authService.currentUser()?.fullName || 'User';
    return name.split(' ').map(p => p[0]).join('').substring(0, 2).toUpperCase();
  }

  onNavigate(): void {
    this.isOpen = false;
  }

  logout(): void {
    this.authService.logout();
  }
}
