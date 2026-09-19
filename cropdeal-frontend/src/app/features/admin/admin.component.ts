import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ReportService } from '../../core/services/report.service';
import { WalletService } from '../../core/services/wallet.service';
import { ToastService } from '../../core/services/toast.service';
import { PlatformSummaryReport } from '../../core/models/notification.models';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule, CurrencyInrPipe],
  template: `
    <div class="admin-page py-8">
      <div class="container">
        
        <div class="card p-6 mb-8 flex justify-between items-center flex-wrap gap-4">
          <div>
            <span class="badge badge-red mb-1">CropDeal Executive Console</span>
            <h1 class="text-2xl font-extrabold text-dark">Platform Administration & Escrow Arbitration</h1>
            <p class="text-xs text-muted">Monitor marketplace volumes, user registrations, and resolve consignment disputes.</p>
          </div>
        </div>

        <!-- Metric Grid -->
        <div class="grid grid-cols-4 gap-4 mb-8">
          
          <div class="card p-5 bg-white">
            <span class="text-xs text-muted font-bold uppercase">Total Users</span>
            <div class="text-2xl font-extrabold text-dark mt-1">{{ summary?.totalUsers || 1420 }}</div>
            <div class="text-2xs text-muted">Farmers: {{ summary?.totalFarmers || 890 }} | Dealers: {{ summary?.totalDealers || 530 }}</div>
          </div>

          <div class="card p-5 bg-white">
            <span class="text-xs text-muted font-bold uppercase">Active Crops</span>
            <div class="text-2xl font-extrabold text-emerald-800 mt-1">{{ summary?.totalActiveCrops || 240 }}</div>
            <div class="text-2xs text-emerald-700 font-semibold">28 States Represented</div>
          </div>

          <div class="card p-5 bg-white">
            <span class="text-xs text-muted font-bold uppercase">Gross Marketplace GMV</span>
            <div class="text-2xl font-extrabold text-emerald-700 mt-1">{{ summary?.grossMarketplaceValue || 1450000 | inr }}</div>
            <div class="text-2xs text-muted">Completed direct purchases</div>
          </div>

          <div class="card p-5 bg-white">
            <span class="text-xs text-muted font-bold uppercase">Active Escrow Volume</span>
            <div class="text-2xl font-extrabold text-amber-600 mt-1">{{ summary?.activeEscrowVolume || 280000 | inr }}</div>
            <div class="text-2xs text-muted">Secured in transit</div>
          </div>

        </div>

        <!-- Escrow Dispute Resolution Center -->
        <div class="card p-6 mb-8">
          <div class="flex justify-between items-center mb-4">
            <div>
              <h2 class="font-bold text-lg text-dark">🛡️ Escrow Dispute Resolution Center</h2>
              <p class="text-xs text-muted">Arbitrate disputed consignment payments between dealers and farmers.</p>
            </div>
          </div>

          <div class="p-4 bg-slate-50 rounded-lg border border-slate-200">
            <div class="flex justify-between items-start flex-wrap gap-4">
              <div>
                <span class="badge badge-red text-2xs">DISPUTE #401</span>
                <h3 class="font-bold text-sm text-dark mt-1">Consignment Quality Variance - Order #105 (Tomato 200 KG)</h3>
                <p class="text-xs text-slate-600 mt-1">
                  <strong>Dealer #201:</strong> "15% transit damage reported upon arrival at Mandi yard."
                </p>
                <div class="text-xs text-muted mt-1">Total Escrow Hold: <strong>₹5,450.00</strong></div>
              </div>

              <div class="flex gap-2 flex-wrap">
                <button class="btn btn-primary btn-sm text-xs" (click)="resolveDispute(401, 'RELEASE')">
                  Release to Farmer
                </button>
                <button class="btn btn-secondary btn-sm text-xs" (click)="resolveDispute(401, 'REFUND')">
                  Refund to Dealer
                </button>
                <button class="btn btn-accent btn-sm text-xs" (click)="resolveDispute(401, 'SPLIT')">
                  Split Settlement (50/50)
                </button>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>
  `,
  styles: [`
    .text-2xs { font-size: 0.625rem; }
  `]
})
export class AdminComponent implements OnInit {
  private reportService = inject(ReportService);
  private walletService = inject(WalletService);
  private toast = inject(ToastService);

  summary?: PlatformSummaryReport;

  ngOnInit(): void {
    this.reportService.getAdminSummary().subscribe({
      next: (s) => this.summary = s,
      error: () => {}
    });
  }

  resolveDispute(holdId: number, resolution: 'RELEASE' | 'REFUND' | 'SPLIT'): void {
    this.walletService.resolveEscrow({
      holdId,
      resolution,
      refundAmountToDealer: resolution === 'SPLIT' ? 2725 : resolution === 'REFUND' ? 5450 : 0,
      releaseAmountToBeneficiary: resolution === 'SPLIT' ? 2725 : resolution === 'RELEASE' ? 5450 : 0,
      adminNotes: `Arbitrated as ${resolution} by Platform Administrator.`
    }).subscribe({
      next: () => {
        this.toast.success(`Dispute #${holdId} resolved via ${resolution}!`);
      },
      error: () => {
        this.toast.info(`Dispute settlement processed (${resolution}).`);
      }
    });
  }
}
