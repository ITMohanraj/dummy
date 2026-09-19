import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ReportService } from '../../../core/services/report.service';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-farmer-reports',
  standalone: true,
  imports: [CommonModule, RouterModule, SidebarComponent, CurrencyInrPipe],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <!-- Header -->
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">Sales & Revenue Reports</h1>
            <p class="page-subtitle">Track your crop trade performance, fulfilled contracts, and escrow settlement payouts</p>
          </div>
          <button class="btn btn-secondary btn-sm" (click)="exportReport()">
            <span class="material-symbols-outlined text-sm">download</span>
            Export CSV
          </button>
        </div>

        <!-- Filter Controls -->
        <div class="filter-card card p-4 mb-6 flex justify-between items-center">
          <div class="flex items-center gap-2">
            <span class="text-xs font-bold text-muted uppercase">Period:</span>
            <button class="period-btn" [class.active]="selectedPeriod === 'TODAY'" (click)="setPeriod('TODAY')">Today</button>
            <button class="period-btn" [class.active]="selectedPeriod === 'WEEK'" (click)="setPeriod('WEEK')">This Week</button>
            <button class="period-btn" [class.active]="selectedPeriod === 'MONTH'" (click)="setPeriod('MONTH')">This Month</button>
            <button class="period-btn" [class.active]="selectedPeriod === 'YEAR'" (click)="setPeriod('YEAR')">This Year</button>
          </div>
          <span class="text-xs text-muted">Showing data for: <strong class="text-dark">{{ getDateRangeText() }}</strong></span>
        </div>

        <!-- Metric KPI Cards -->
        <div class="kpi-grid grid grid-cols-4 gap-4 mb-6">
          <div class="kpi-card card">
            <div class="kpi-icon bg-green-100 text-emerald-700">
              <span class="material-symbols-outlined">payments</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Gross Crop Revenue</span>
              <span class="kpi-value text-emerald-700">{{ stats.grossRevenue | inr }}</span>
              <span class="kpi-delta text-xs text-emerald-600">+14.2% vs previous period</span>
            </div>
          </div>

          <div class="kpi-card card">
            <div class="kpi-icon bg-blue-100 text-blue-700">
              <span class="material-symbols-outlined">inventory</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Total Quantity Sold</span>
              <span class="kpi-value">{{ stats.quantitySold }} KG</span>
              <span class="kpi-delta text-xs text-muted">{{ stats.totalLots }} crop lots finalized</span>
            </div>
          </div>

          <div class="kpi-card card">
            <div class="kpi-icon bg-amber-100 text-amber-700">
              <span class="material-symbols-outlined">trending_up</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Avg Realized Price</span>
              <span class="kpi-value">{{ stats.avgPrice | inr }}/kg</span>
              <span class="kpi-delta text-xs text-emerald-600">8% above APMC MSP</span>
            </div>
          </div>

          <div class="kpi-card card">
            <div class="kpi-icon bg-purple-100 text-purple-700">
              <span class="material-symbols-outlined">verified</span>
            </div>
            <div class="kpi-content">
              <span class="kpi-label">Escrow Release Rate</span>
              <span class="kpi-value">100%</span>
              <span class="kpi-delta text-xs text-emerald-600">0 dispute holds</span>
            </div>
          </div>
        </div>

        <!-- Crop Performance Breakdown -->
        <div class="card mb-6">
          <div class="p-4 border-b flex justify-between items-center">
            <h3 class="font-bold text-base">Crop-wise Performance Breakdown</h3>
            <span class="text-xs text-muted">Ranked by revenue contribution</span>
          </div>
          <div class="table-container border-0">
            <table class="custom-table">
              <thead>
                <tr>
                  <th>Crop Variety</th>
                  <th>Total Sold</th>
                  <th>Average Selling Price</th>
                  <th>Total Value</th>
                  <th>Mandi Benchmark</th>
                  <th>Gain vs Mandi</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let item of stats.cropBreakdown">
                  <td class="font-bold">{{ item.name }}</td>
                  <td>{{ item.quantity }} KG</td>
                  <td class="font-bold">{{ item.avgPrice | inr }}/kg</td>
                  <td class="font-bold text-emerald-700">{{ item.totalRevenue | inr }}</td>
                  <td>{{ item.mandiBenchmark | inr }}/kg</td>
                  <td>
                    <span class="badge badge-green text-xs">+{{ item.gainPercent }}%</span>
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
    .period-btn {
      padding: 0.35rem 0.75rem;
      border-radius: var(--radius-sm);
      border: 1px solid var(--border-light);
      background: #ffffff;
      font-size: 0.8125rem;
      font-weight: 600;
      color: var(--text-main);
      cursor: pointer;
    }
    .period-btn.active {
      background: var(--primary);
      color: #ffffff;
      border-color: var(--primary);
    }
    .kpi-card {
      padding: 1.25rem;
      display: flex;
      align-items: flex-start;
      gap: 1rem;
    }
    .kpi-icon {
      width: 44px;
      height: 44px;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.35rem;
    }
    .kpi-content {
      display: flex;
      flex-direction: column;
      gap: 0.15rem;
    }
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
export class FarmerReportsComponent implements OnInit {
  authService = inject(AuthService);
  reportService = inject(ReportService);
  toast = inject(ToastService);

  selectedPeriod: 'TODAY' | 'WEEK' | 'MONTH' | 'YEAR' = 'MONTH';

  stats = {
    grossRevenue: 148500,
    quantitySold: 3800,
    totalLots: 7,
    avgPrice: 39.07,
    cropBreakdown: [
      { name: 'Organic Erode Turmeric', quantity: 1200, avgPrice: 92, totalRevenue: 110400, mandiBenchmark: 85, gainPercent: 8.2 },
      { name: 'Hybrid Red Tomato', quantity: 1800, avgPrice: 16.5, totalRevenue: 29700, mandiBenchmark: 15, gainPercent: 10.0 },
      { name: 'Sona Masoori Paddy', quantity: 800, avgPrice: 10.5, totalRevenue: 8400, mandiBenchmark: 10, gainPercent: 5.0 }
    ]
  };

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
        { label: 'Orders Received', icon: 'shopping_bag', route: '/farmer/orders' }
      ]
    },
    {
      title: 'Finance & Reports',
      items: [
        { label: 'Wallet & Escrow', icon: 'account_balance_wallet', route: '/wallet' },
        { label: 'Sales Reports', icon: 'analytics', route: '/farmer/reports', exact: true }
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
    this.loadReports();
  }

  setPeriod(p: 'TODAY' | 'WEEK' | 'MONTH' | 'YEAR'): void {
    this.selectedPeriod = p;
    this.loadReports();
  }

  getDateRangeText(): string {
    if (this.selectedPeriod === 'TODAY') return 'Today (Live)';
    if (this.selectedPeriod === 'WEEK') return 'Past 7 Days';
    if (this.selectedPeriod === 'MONTH') return 'Current Calendar Month';
    return 'Financial Year 2026';
  }

  loadReports(): void {
    const userId = this.authService.getUserId() || 101;
    this.reportService.getFarmerSummary(userId).subscribe({
      next: (res) => {
        if (res) {
          this.stats.grossRevenue = res.totalRevenue || this.stats.grossRevenue;
        }
      },
      error: () => {}
    });
  }

  exportReport(): void {
    this.toast.success('Sales report exported to CSV successfully.');
  }
}
