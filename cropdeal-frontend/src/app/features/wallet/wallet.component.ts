import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { WalletService } from '../../core/services/wallet.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { DeliveryEscrowHold, DomainEvent, UserWallet, WalletTransaction } from '../../core/models/wallet.models';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-wallet',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule, 
    LoadingSpinnerComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="wallet-page py-8">
      <div class="container">
        
        <!-- Header -->
        <div class="flex justify-between items-center mb-6 flex-wrap gap-4">
          <div>
            <span class="badge badge-green mb-1">Production Escrow & Event Store</span>
            <h1 class="text-2xl font-extrabold text-dark">CropDeal Digital Wallet</h1>
            <p class="text-xs text-muted">Secured by multi-stage Escrow and immutable Event Sourcing ledger.</p>
          </div>

          <div class="flex gap-3">
            <button class="btn btn-primary btn-sm" (click)="showTopUpModal = true">
              <span class="material-symbols-outlined text-sm">add_card</span>
              Top Up Balance
            </button>
            <button class="btn btn-secondary btn-sm" (click)="showWithdrawModal = true">
              <span class="material-symbols-outlined text-sm">payments</span>
              Withdraw
            </button>
          </div>
        </div>

        <!-- 3 Wallet Balance Cards -->
        <div class="grid grid-cols-3 gap-6 mb-8">
          
          <div class="card p-6 border-l-4 border-emerald-600 bg-white">
            <span class="text-xs text-muted font-bold uppercase">Available Balance</span>
            <div class="text-3xl font-extrabold text-emerald-800 my-2">
              {{ wallet?.balance || 0 | inr }}
            </div>
            <div class="text-2xs text-muted">Instant withdrawal & crop procurement</div>
          </div>

          <div class="card p-6 border-l-4 border-amber-500 bg-white">
            <span class="text-xs text-muted font-bold uppercase">Escrow Locked Funds</span>
            <div class="text-3xl font-extrabold text-amber-700 my-2">
              {{ wallet?.escrowBalance || 0 | inr }}
            </div>
            <div class="text-2xs text-muted">Protected in active order consignments</div>
          </div>

          <div class="card p-6 border-l-4 border-blue-600 bg-white">
            <span class="text-xs text-muted font-bold uppercase">Total Lifetime Flow</span>
            <div class="text-3xl font-extrabold text-dark my-2">
              {{ (wallet?.totalEarnings || 0) + (wallet?.totalSpent || 0) | inr }}
            </div>
            <div class="text-2xs text-muted">Earnings: {{ wallet?.totalEarnings || 0 | inr }} | Spent: {{ wallet?.totalSpent || 0 | inr }}</div>
          </div>

        </div>

        <!-- Escrow Holds Management Section -->
        <div class="card p-6 mb-8">
          <div class="flex justify-between items-center mb-4">
            <div>
              <h2 class="font-bold text-lg text-dark">Active Escrow Consignment Holds</h2>
              <p class="text-xs text-muted">Production escrow engine ensuring funds remain secure until delivery verification.</p>
            </div>
          </div>

          <div *ngIf="escrows.length === 0" class="text-sm text-muted py-4 text-center">
            No active escrow holds on your account.
          </div>

          <div *ngIf="escrows.length > 0" class="table-container">
            <table class="custom-table">
              <thead>
                <tr>
                  <th>Hold ID</th>
                  <th>Order / Trip Ref</th>
                  <th>Beneficiary</th>
                  <th>Hold Type</th>
                  <th>Amount</th>
                  <th>Escrow State</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                <tr *ngFor="let e of escrows">
                  <td>#{{ e.id }}</td>
                  <td>{{ e.orderId ? 'Order #' + e.orderId : 'Trip #' + e.deliveryId }}</td>
                  <td>User #{{ e.beneficiaryId }}</td>
                  <td><span class="badge badge-gray text-2xs">{{ e.holdType }}</span></td>
                  <td class="font-bold text-emerald-800">{{ e.amount | inr }}</td>
                  <td>
                    <span class="badge" [ngClass]="e.status === 'RELEASED' ? 'badge-green' : e.status === 'HELD' ? 'badge-gold' : 'badge-red'">
                      {{ e.status }}
                    </span>
                  </td>
                  <td>
                    <button 
                      *ngIf="e.status === 'HELD'"
                      class="btn btn-secondary btn-sm text-xs"
                      (click)="disputeEscrow(e.id)"
                    >
                      Raise Dispute
                    </button>
                    <span *ngIf="e.status !== 'HELD'" class="text-xs text-muted">Processed</span>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- Tabs: Transactions vs Immutable Event Store Stream -->
        <div class="card p-6">
          <div class="flex gap-4 border-b border-slate-200 pb-3 mb-4">
            <button 
              class="font-bold text-sm" 
              [class.text-emerald-700]="activeTab === 'tx'"
              [class.text-muted]="activeTab !== 'tx'"
              (click)="activeTab = 'tx'"
            >
              Transaction Ledger
            </button>
            <button 
              class="font-bold text-sm" 
              [class.text-emerald-700]="activeTab === 'events'"
              [class.text-muted]="activeTab !== 'events'"
              (click)="activeTab = 'events'"
            >
              ⚡ Immutable Domain Event Stream (Event Sourcing)
            </button>
          </div>

          <!-- Transaction Table -->
          <div *ngIf="activeTab === 'tx'">
            <div *ngIf="transactions.length === 0" class="text-sm text-muted py-4 text-center">
              No transactions recorded yet.
            </div>

            <div *ngIf="transactions.length > 0" class="table-container">
              <table class="custom-table">
                <thead>
                  <tr>
                    <th>Tx ID</th>
                    <th>Type</th>
                    <th>Description</th>
                    <th>Amount</th>
                    <th>Balance After</th>
                    <th>Date & Time</th>
                  </tr>
                </thead>
                <tbody>
                  <tr *ngFor="let t of transactions">
                    <td>#{{ t.transactionId }}</td>
                    <td>
                      <span class="badge" [ngClass]="t.transactionType === 'CREDIT' ? 'badge-green' : 'badge-gold'">
                        {{ t.transactionType }}
                      </span>
                    </td>
                    <td class="text-xs">{{ t.description }}</td>
                    <td class="font-bold" [ngClass]="t.transactionType === 'CREDIT' ? 'text-emerald-700' : 'text-slate-800'">
                      {{ t.transactionType === 'CREDIT' ? '+' : '-' }}{{ t.amount | inr }}
                    </td>
                    <td class="text-xs">{{ t.balanceAfter | inr }}</td>
                    <td class="text-xs text-muted">{{ t.createdAt | date:'medium' }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>

          <!-- Event Sourcing Event Stream -->
          <div *ngIf="activeTab === 'events'">
            <div *ngIf="domainEvents.length === 0" class="text-sm text-muted py-4 text-center">
              No domain events recorded in the Event Store for this wallet.
            </div>

            <div *ngIf="domainEvents.length > 0" class="event-stream-list flex flex-col gap-3">
              <div *ngFor="let ev of domainEvents" class="event-item p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs">
                <div class="flex justify-between items-center mb-1">
                  <span class="font-bold text-emerald-800">{{ ev.eventType }}</span>
                  <span class="text-muted">v{{ ev.version }} • {{ ev.occurredOn | date:'mediumTime' }}</span>
                </div>
                <div class="font-mono text-2xs text-slate-600 bg-white p-2 rounded border border-slate-100">
                  {{ ev.eventData }}
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- Top Up Modal -->
        <div *ngIf="showTopUpModal" class="modal-backdrop">
          <div class="modal-card card p-6">
            <h3 class="font-bold text-lg mb-2">Top Up CropDeal Wallet</h3>
            <p class="text-xs text-muted mb-4">Add funds instantly via UPI, NetBanking, or Debit Card.</p>
            
            <div class="form-group">
              <label class="form-label">Amount (₹)</label>
              <input type="number" [(ngModel)]="topUpAmount" class="form-control" />
            </div>

            <div class="flex justify-end gap-3 mt-6">
              <button class="btn btn-secondary btn-sm" (click)="showTopUpModal = false">Cancel</button>
              <button class="btn btn-primary btn-sm" (click)="executeTopUp()">Add Funds</button>
            </div>
          </div>
        </div>

        <!-- Withdraw Modal -->
        <div *ngIf="showWithdrawModal" class="modal-backdrop">
          <div class="modal-card card p-6">
            <h3 class="font-bold text-lg mb-2">Withdraw to Bank Account</h3>
            <p class="text-xs text-muted mb-4">Transfer available funds to your verified bank account.</p>
            
            <div class="form-group">
              <label class="form-label">Withdrawal Amount (₹)</label>
              <input type="number" [(ngModel)]="withdrawAmount" class="form-control" />
            </div>

            <div class="flex justify-end gap-3 mt-6">
              <button class="btn btn-secondary btn-sm" (click)="showWithdrawModal = false">Cancel</button>
              <button class="btn btn-primary btn-sm" (click)="executeWithdraw()">Transfer to Bank</button>
            </div>
          </div>
        </div>

      </div>
    </div>
  `,
  styles: [`
    .text-2xs { font-size: 0.625rem; }
    .modal-backdrop {
      position: fixed;
      inset: 0;
      background: rgba(15, 23, 42, 0.6);
      backdrop-filter: blur(4px);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 2000;
    }
    .modal-card {
      width: 100%;
      max-width: 420px;
    }
  `]
})
export class WalletComponent implements OnInit {
  private walletService = inject(WalletService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);

  wallet?: UserWallet;
  transactions: WalletTransaction[] = [];
  escrows: DeliveryEscrowHold[] = [];
  domainEvents: DomainEvent[] = [];

  activeTab: 'tx' | 'events' = 'tx';
  showTopUpModal = false;
  showWithdrawModal = false;

  topUpAmount = 5000;
  withdrawAmount = 2000;

  ngOnInit(): void {
    const userId = this.authService.getUserId() || 101;
    this.loadWallet(userId);
  }

  loadWallet(userId: number): void {
    const role = this.authService.getUserRole() || 'FARMER';
    this.walletService.getWallet(userId, role).subscribe({
      next: (w) => this.wallet = w,
      error: () => {}
    });

    this.walletService.getTransactions(userId).subscribe({
      next: (txs) => this.transactions = txs || [],
      error: () => {}
    });

    this.walletService.getUserEscrows(userId).subscribe({
      next: (e) => this.escrows = e || [],
      error: () => {}
    });

    this.walletService.getEventStream(userId).subscribe({
      next: (evs) => this.domainEvents = evs || [],
      error: () => {}
    });
  }

  executeTopUp(): void {
    const userId = this.authService.getUserId() || 101;
    this.walletService.topUp(userId, this.topUpAmount).subscribe({
      next: () => {
        this.toast.success(`₹${this.topUpAmount} added to your CropDeal Wallet!`);
        this.showTopUpModal = false;
        this.loadWallet(userId);
      },
      error: () => {}
    });
  }

  executeWithdraw(): void {
    const userId = this.authService.getUserId() || 101;
    this.walletService.debit(userId, this.withdrawAmount, 'BANK_PAYOUT').subscribe({
      next: () => {
        this.toast.success(`₹${this.withdrawAmount} withdrawal initiated to your registered bank account!`);
        this.showWithdrawModal = false;
        this.loadWallet(userId);
      },
      error: () => {}
    });
  }

  disputeEscrow(holdId: number): void {
    const userId = this.authService.getUserId() || 101;
    this.walletService.disputeEscrow({
      holdId,
      reason: 'Consignment quality variance or transit delay reported.',
      raisedByUserId: userId
    }).subscribe({
      next: () => {
        this.toast.warning('Dispute raised. Platform admin notified for resolution.');
        this.loadWallet(userId);
      },
      error: () => {}
    });
  }
}
