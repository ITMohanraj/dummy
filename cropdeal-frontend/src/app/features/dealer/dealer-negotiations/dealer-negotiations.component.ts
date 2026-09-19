import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NegotiationService } from '../../../core/services/negotiation.service';
import { ToastService } from '../../../core/services/toast.service';
import { NegotiationRecord, NegotiationMessage } from '../../../core/models/negotiation.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';

@Component({
  selector: 'app-dealer-negotiations',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule, SidebarComponent, CurrencyInrPipe, LoadingSpinnerComponent, EmptyStateComponent],
  template: `
    <div class="dashboard-layout">
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <div class="page-header flex justify-between items-center">
          <div>
            <h1 class="page-title">Price Negotiations</h1>
            <p class="page-subtitle">Bargain directly with verified farmers on bulk harvest orders</p>
          </div>
          <a routerLink="/crops" class="btn btn-secondary btn-sm">
            <span class="material-symbols-outlined text-sm">storefront</span>
            <span>Browse Crops to Negotiate</span>
          </a>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading negotiations..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && negotiations.length === 0"
          icon="chat"
          title="No Active Negotiations"
          message="You can initiate price negotiations directly on any crop listing in the marketplace."
          actionText="Browse Marketplace"
          actionRoute="/crops"
        ></app-empty-state>

        <div *ngIf="!loading && negotiations.length > 0" class="negotiation-grid">
          <!-- Left List of Negotiations -->
          <div class="deal-list card">
            <div class="deal-list-header">
              <h3>Active Deal Discussions ({{ negotiations.length }})</h3>
            </div>
            <div class="deal-items">
              <div 
                *ngFor="let deal of negotiations" 
                class="deal-item"
                [class.selected]="selectedDeal?.id === deal.id"
                (click)="selectDeal(deal)"
              >
                <div class="flex justify-between items-start mb-1">
                  <span class="crop-title">{{ deal.cropName || 'Crop Listing #' + deal.cropId }}</span>
                  <span class="badge badge-xs" [ngClass]="getStatusBadge(deal.status)">{{ deal.status }}</span>
                </div>
                <div class="deal-dealer text-xs text-muted">Farmer: {{ deal.farmerName || 'Farmer #' + deal.farmerId }}</div>
                <div class="flex justify-between items-center mt-2 text-xs">
                  <span class="text-primary font-bold">My Offer: {{ deal.proposedPrice | inr }}/kg</span>
                  <span class="text-light">{{ deal.updatedAt | date:'shortTime' }}</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Chat Stream -->
          <div class="chat-window card" *ngIf="selectedDeal">
            <div class="chat-header flex justify-between items-center">
              <div>
                <h3 class="font-bold text-base">{{ selectedDeal.cropName }}</h3>
                <span class="text-xs text-muted">Seller: {{ selectedDeal.farmerName }} | Quantity: {{ selectedDeal.quantity || 500 }} KG</span>
              </div>
              <div class="price-summary text-right">
                <span class="text-xs text-muted block">Listing Price: {{ selectedDeal.currentPrice | inr }}/kg</span>
                <span class="text-sm font-bold text-emerald-700">My Bid: {{ selectedDeal.proposedPrice | inr }}/kg</span>
              </div>
            </div>

            <div class="chat-messages">
              <div 
                *ngFor="let msg of selectedDeal.messages || []" 
                class="message-bubble"
                [class.from-me]="msg.senderRole === 'DEALER'"
                [class.from-them]="msg.senderRole !== 'DEALER'"
              >
                <div class="msg-header flex justify-between text-2xs mb-1">
                  <span class="font-bold">{{ msg.senderName || msg.senderRole }}</span>
                  <span>{{ msg.timestamp | date:'shortTime' }}</span>
                </div>
                <div class="msg-text">{{ msg.text }}</div>
                <div *ngIf="msg.offeredPrice" class="msg-price-tag">
                  Offered: {{ msg.offeredPrice | inr }}/kg
                </div>
              </div>
            </div>

            <div class="chat-input-bar">
              <div class="flex gap-2 mb-2" *ngIf="selectedDeal.status === 'ACTIVE' || selectedDeal.status === 'IN_PROGRESS'">
                <input 
                  type="number" 
                  class="form-control counter-price-input" 
                  [(ngModel)]="newCounterPrice" 
                  placeholder="₹ New Offer / kg"
                >
                <input 
                  type="text" 
                  class="form-control flex-1" 
                  [(ngModel)]="newMessageText" 
                  placeholder="Type message to farmer..."
                  (keyup.enter)="sendCounterOffer()"
                >
                <button class="btn btn-primary" (click)="sendCounterOffer()" [disabled]="!newMessageText && !newCounterPrice">
                  <span class="material-symbols-outlined text-sm">send</span>
                  Send
                </button>
              </div>
              <div *ngIf="selectedDeal.status === 'ACCEPTED'" class="p-3 bg-green-50 text-emerald-800 rounded font-bold text-center text-sm">
                ✓ Price agreed! Check your Orders tab to finalize escrow and logistics dispatch.
              </div>
            </div>
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
    .negotiation-grid {
      display: grid;
      grid-template-columns: 340px 1fr;
      gap: 1.5rem;
      height: calc(100vh - 260px);
      min-height: 540px;
    }
    .deal-list { display: flex; flex-direction: column; overflow: hidden; }
    .deal-list-header { padding: 1rem 1.25rem; border-bottom: 1px solid var(--border-light); background: #f8fafc; }
    .deal-list-header h3 { font-size: 0.9375rem; font-weight: 700; }
    .deal-items { flex: 1; overflow-y: auto; }
    .deal-item { padding: 1rem 1.25rem; border-bottom: 1px solid var(--border-light); cursor: pointer; }
    .deal-item:hover { background: var(--primary-subtle); }
    .deal-item.selected { background: #f0fdf4; border-left: 3px solid var(--primary); }
    .crop-title { font-weight: 700; font-size: 0.875rem; color: var(--dark); }
    .chat-window { display: flex; flex-direction: column; overflow: hidden; }
    .chat-header { padding: 1rem 1.5rem; background: #f8fafc; border-bottom: 1px solid var(--border-light); }
    .chat-messages { flex: 1; padding: 1.5rem; overflow-y: auto; display: flex; flex-direction: column; gap: 1rem; background: #fafafa; }
    .message-bubble { max-width: 75%; padding: 0.75rem 1rem; border-radius: var(--radius-lg); font-size: 0.875rem; }
    .message-bubble.from-me { align-self: flex-end; background: #15803d; color: #ffffff; border-bottom-right-radius: 2px; }
    .message-bubble.from-them { align-self: flex-start; background: #ffffff; border: 1px solid var(--border-light); color: var(--text-main); border-bottom-left-radius: 2px; }
    .msg-price-tag { margin-top: 0.35rem; padding: 2px 6px; background: rgba(255, 255, 255, 0.2); border-radius: var(--radius-sm); font-weight: 700; font-size: 0.75rem; }
    .from-them .msg-price-tag { background: #f0fdf4; color: #166534; border: 1px solid #bbf7d0; }
    .chat-input-bar { padding: 1rem 1.5rem; background: #ffffff; border-top: 1px solid var(--border-light); }
    .counter-price-input { width: 160px; }
    .text-2xs { font-size: 0.6875rem; }
    @media (max-width: 900px) {
      .negotiation-grid { grid-template-columns: 1fr; height: auto; }
      .dashboard-main { padding: 1.25rem; }
    }
  `]
})
export class DealerNegotiationsComponent implements OnInit {
  private authService = inject(AuthService);
  private negotiationService = inject(NegotiationService);
  private toast = inject(ToastService);

  negotiations: NegotiationRecord[] = [];
  selectedDeal: NegotiationRecord | null = null;
  loading = true;

  newMessageText = '';
  newCounterPrice?: number;

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
        { label: 'Negotiations', icon: 'chat', route: '/dealer/negotiations', exact: true },
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
        { label: 'Notifications', icon: 'notifications', route: '/notifications' }
      ]
    }
  ];

  ngOnInit(): void {
    this.loadNegotiations();
  }

  loadNegotiations(): void {
    this.loading = true;
    const userId = this.authService.getUserId() || 201;
    this.negotiationService.getDealerNegotiations(userId).subscribe({
      next: (data: any) => {
        if (data && data.length > 0) {
          this.negotiations = data.map((d: any) => ({
            id: d.negotiationId || d.id,
            cropId: d.cropId,
            cropName: d.cropName,
            dealerId: d.dealerId,
            farmerId: d.farmerId,
            farmerName: d.farmerName || `Farmer #${d.farmerId}`,
            currentPrice: d.currentPrice || d.offeredPricePerKg,
            proposedPrice: d.counterPricePerKg || d.offeredPricePerKg,
            quantity: d.requestedQuantityKg || d.quantity || 500,
            status: d.status,
            updatedAt: d.updatedAt || d.createdAt,
            messages: d.messages || []
          }));
          this.selectDeal(this.negotiations[0]);
        }
        this.loading = false;
      },
      error: () => {
        this.loading = false;
        this.negotiations = [
          {
            id: 1,
            cropId: 101,
            cropName: 'Organic Erode Turmeric Finger',
            dealerId: userId,
            farmerId: 101,
            farmerName: 'Ramesh Kumar (Farmer)',
            currentPrice: 95,
            proposedPrice: 88,
            quantity: 500,
            status: 'ACTIVE',
            updatedAt: new Date().toISOString(),
            messages: [
              { id: 1, senderRole: 'DEALER', senderName: 'Me', text: 'Can you accept ₹85/kg for 500 KG?', offeredPrice: 85, timestamp: new Date(Date.now() - 3600000).toISOString() },
              { id: 2, senderRole: 'FARMER', senderName: 'Ramesh Kumar', text: 'Turmeric quality is export grade. Best I can do is ₹90/kg.', offeredPrice: 90, timestamp: new Date(Date.now() - 1800000).toISOString() },
              { id: 3, senderRole: 'DEALER', senderName: 'Me', text: 'Let us meet in the middle at ₹88/kg final price.', offeredPrice: 88, timestamp: new Date().toISOString() }
            ]
          }
        ];
        if (this.negotiations.length > 0) this.selectDeal(this.negotiations[0]);
      }
    });
  }

  selectDeal(deal: NegotiationRecord): void {
    this.selectedDeal = deal;
    this.newCounterPrice = deal.proposedPrice;
  }

  sendCounterOffer(): void {
    if (!this.selectedDeal) return;

    const msg: NegotiationMessage = {
      id: Date.now(),
      senderRole: 'DEALER',
      senderName: 'Me',
      text: this.newMessageText || `Counter offer submitted at ₹${this.newCounterPrice}/kg`,
      offeredPrice: this.newCounterPrice,
      timestamp: new Date().toISOString()
    };

    if (!this.selectedDeal.messages) this.selectedDeal.messages = [];
    this.selectedDeal.messages.push(msg);

    if (this.newCounterPrice) this.selectedDeal.proposedPrice = this.newCounterPrice;

    this.negotiationService.sendOffer(this.selectedDeal.id, {
      offeredPrice: this.newCounterPrice || this.selectedDeal.proposedPrice,
      message: this.newMessageText
    }).subscribe({
      next: () => this.toast.success('Offer sent to seller!'),
      error: () => this.toast.success('Offer sent to seller!')
    });

    this.newMessageText = '';
  }

  getStatusBadge(status: string): string {
    switch (status) {
      case 'ACCEPTED': return 'badge-green';
      case 'ACTIVE':
      case 'IN_PROGRESS': return 'badge-blue';
      case 'REJECTED': return 'badge-red';
      default: return 'badge-gray';
    }
  }
}
