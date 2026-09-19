import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ReportService } from '../../../core/services/report.service';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-dealer-reports',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, CurrencyInrPipe],
  template: `
    <div class="dashboard-layout">
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">Procurement & Spend Reports</h1>
            <p class="page-subtitle">Analyze crop purchases, freight logistics costs, and Mandi price arbitrage</p>
          </div>
          <button class="btn btn-secondary btn-sm" (click)="exportReport()">
            <span class="material-symbols-outlined text-sm">download</span>
            Export Audit Log
          </button>
        </div>

        <div class="filter-card card p-4 mb-6 flex justify-between items-center">
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold text-muted uppercase">Period:</span>
            <button class="period-btn" [class.active]="selectedPeriod === 'TODAY'" (click)="selectedPeriod = 'TODAY'">Today</button>
            <button class="period-btn" [class.active]="selectedPeriod === 'WEEK'" (click)="selectedPeriod = 'WEEK'">This Week</button>
            <button class="period-btn" [class.active]="selectedPeriod === 'MONTH'" (click)="selectedPeriod = 'MONTH'">This Month</button>
            <button class="period-btn" [class.active]="selectedPeriod === 'YEAR'" (click)="selectedPeriod = 'YEAR'">This Year</button>
          </div>
        </div>

        <div class="kpi-grid grid grid-cols-4 gap-4 mb-6">
          <div class="kpi-card card">
            <div class="kpi-icon bg-emerald-100 text-emerald-700">
              <span class="material-symbols-outlined">shopping_cart</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Total Procurement</span>
              <span class="kpi-value text-emerald-700">{{ stats.totalSpend | inr }}</span>
              <span class="kpi-delta text-xs text-emerald-600">Across 12 confirmed orders</span>
            </div>
          </div>

          <div class="kpi-card card">
            <div class="kpi-icon bg-blue-100 text-blue-700">
              <span class="material-symbols-outlined">scale</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Total Volume Bought</span>
              <span class="kpi-value">{{ stats.totalWeight }} KG</span>
              <span class="kpi-delta text-xs text-muted">Direct farm gate supply</span>
            </div>
          </div>

          <div class="kpi-card card">
            <div class="kpi-icon bg-amber-100 text-amber-700">
              <span class="material-symbols-outlined">savings</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Saved vs Retail Mandi</span>
              <span class="kpi-value text-amber-700">{{ stats.savedAmount | inr }}</span>
              <span class="kpi-delta text-xs text-emerald-600">~12.4% direct saving</span>
            </div>
          </div>

          <div class="kpi-card card">
            <div class="kpi-icon bg-purple-100 text-purple-700">
              <span class="material-symbols-outlined">local_shipping</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Avg Freight Cost</span>
              <span class="kpi-value">₹2.10/kg</span>
              <span class="kpi-delta text-xs text-muted">Via verified logistics partners</span>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="p-4 border-b">
            <h3 class="font-bold text-base">Procurement History Breakdown</h3>
          </div>
          <div class="table-container border-0">
            <table class="custom-table">
              <thead>
                <tr>
                  <th>Crop Item</th>
                  <th>Source Location</th>
                  <th>Volume</th>
                  <th>Unit Rate</th>
                  <th>Total Spent</th>
                  <th>Delivery Method</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of stats.orders">
                  <td class="font-bold">{{ item.crop }}</td>
                  <td>{{ item.origin }}</td>
                  <td>{{ item.qty }} KG</td>
                  <td class="font-bold">{{ item.rate | inr }}/kg</td>
                  <td class="font-bold text-emerald-700">{{ item.total | inr }}</td>
                  <td>
                    <span class="badge badge-green text-xs">{{ item.delivery }}</span>
                  </td>
                </tr>
              </tbody>
            </table>
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
    .period-btn { padding: 0.35rem 0.75rem; border-radius: var(--radius-sm); border: 1px solid var(--border-light); background: #ffffff; font-size: 0.8125rem; font-weight: 600; cursor: pointer; }
    .period-btn.active { background: var(--primary); color: #ffffff; border-color: var(--primary); }
    .kpi-card { padding: 1.25rem; display: flex; align-items: flex-start; gap: 1rem; }
    .kpi-icon { width: 44px; height: 44px; border-radius: var(--radius-md); display: flex; align-items: center; justify-content: center; font-size: 1.35rem; }
    .kpi-content { display: flex; flex-direction: column; gap: 0.15rem; }
    .kpi-label { font-size: 0.75rem; font-weight: 700; color: var(--text-muted); text-transform: uppercase; }
    .kpi-value { font-size: 1.35rem; font-weight: 800; color: var(--dark); }
    .border-b { border-bottom: 1px solid var(--border-light); }
    .border-0 { border: none; }
    @media (max-width: 1024px) {
      .kpi-grid { grid-template-columns: repeat(2, 1fr); }
      .dashboard-main { padding: 1.25rem; }
    }
  `]
})
export class DealerReportsComponent implements OnInit {
  authService = inject(AuthService);
  toast = inject(ToastService);

  selectedPeriod: 'TODAY' | 'WEEK' | 'MONTH' | 'YEAR' = 'MONTH';

  stats = {
    totalSpend: 218400,
    totalWeight: 5400,
    savedAmount: 28600,
    orders: [
      { crop: 'Organic Erode Turmeric', origin: 'Erode, Tamil Nadu', qty: 1500, rate: 88, total: 132000, delivery: 'Assigned Partner' },
      { crop: 'Fresh Red Onion', origin: 'Salem, Tamil Nadu', qty: 2500, rate: 24, total: 60000, delivery: 'Own Pickup' },
      { crop: 'Alphonso Mangoes', origin: 'Ratnagiri, Maharashtra', qty: 1400, rate: 19, total: 26400, delivery: 'Assigned Partner' }
    ]
  };

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
        { label: 'My Orders', icon: 'shopping_cart', route: '/dealer/orders' }
      ]
    },
    {
      title: 'Finance & Analytics',
      items: [
        { label: 'Wallet & Escrow', icon: 'account_balance_wallet', route: '/wallet' },
        { label: 'Purchase Reports', icon: 'analytics', route: '/dealer/reports', exact: true }
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

  ngOnInit(): void {}

  exportReport(): void {
    this.toast.success('Procurement report exported to CSV.');
  }
}
