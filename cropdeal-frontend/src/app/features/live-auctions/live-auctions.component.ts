import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { BiddingService } from '../../core/services/bidding.service';
import { AuthService } from '../../core/services/auth.service';
import { ToastService } from '../../core/services/toast.service';
import { AuctionBid, CropAuction } from '../../core/models/bidding.models';
import { LoadingSpinnerComponent } from '../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../shared/pipes/currency-inr.pipe';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-live-auctions',
  standalone: true,
  imports: [
    CommonModule, 
    RouterModule, 
    FormsModule, 
    LoadingSpinnerComponent, 
    EmptyStateComponent, 
    CurrencyInrPipe
  ],
  template: `
    <div class="auctions-page py-8">
      <div class="container">
        
        <!-- Header -->
        <div class="flex justify-between items-center flex-wrap gap-4 mb-8">
          <div>
            <div class="flex items-center gap-2 mb-1">
              <span class="badge badge-gold">Real-time STOMP Floor</span>
              <span class="badge badge-green">Live Bidding Active</span>
            </div>
            <h1 class="page-title">Live Crop Bidding Auctions</h1>
            <p class="text-sm text-muted">Compete in open, transparent live auctions for bulk harvest consignments</p>
          </div>

          <div class="flex gap-3">
            <button 
              *ngIf="authService.getUserRole() === 'FARMER'" 
              class="btn btn-accent btn-sm"
              (click)="showCreateModal = true"
            >
              <span class="material-symbols-outlined text-sm">add_circle</span>
              Start New Auction
            </button>
          </div>
        </div>

        <app-loading-spinner *ngIf="loading" message="Connecting to live auction stream..."></app-loading-spinner>

        <!-- Auctions Grid -->
        <div *ngIf="!loading && auctions.length > 0" class="auctions-grid">
          <div *ngFor="let auction of auctions" class="auction-card card p-5">
            
            <div class="flex justify-between items-start mb-3">
              <div>
                <span class="badge badge-green text-2xs">LIVE AUCTION #{{ auction.auctionId }}</span>
                <h3 class="font-bold text-lg text-dark mt-1">{{ auction.cropName }}</h3>
                <div class="text-xs text-muted">Consignment: {{ auction.quantityKg }} KG</div>
              </div>
              <div class="live-pulse">
                <span class="pulse-dot"></span>
                <span class="text-xs font-bold text-emerald-700">LIVE</span>
              </div>
            </div>

            <!-- Price & Bid Stats -->
            <div class="bid-stats-box p-4 bg-slate-50 rounded-lg border border-slate-200 mb-4">
              <div class="flex justify-between items-center mb-2">
                <span class="text-xs text-muted">Current Highest Bid:</span>
                <span class="highest-bid-val text-emerald-800 font-extrabold text-lg">
                  {{ (auction.currentHighestBid || auction.startingPricePerKg) | inr }} / KG
                </span>
              </div>
              <div class="flex justify-between text-2xs text-muted">
                <span>Starting Base: {{ auction.startingPricePerKg | inr }}/KG</span>
                <span>Total Bids: <strong>{{ auction.totalBidsCount }}</strong></span>
              </div>
            </div>

            <!-- Bid Input Action -->
            <div class="bid-action flex gap-2">
              <input 
                type="number" 
                [(ngModel)]="bidAmounts[auction.auctionId]" 
                [placeholder]="'Min ' + ((auction.currentHighestBid || auction.startingPricePerKg) + 1)"
                class="form-control"
              />
              <button 
                class="btn btn-primary btn-sm"
                (click)="submitBid(auction)"
              >
                Place Bid
              </button>
            </div>

            <!-- Farmer Acceptance Option -->
            <div 
              *ngIf="authService.getUserRole() === 'FARMER' && auction.farmerId === authService.getUserId()" 
              class="mt-3 pt-3 border-t border-slate-100"
            >
              <button 
                class="btn btn-secondary btn-sm w-full"
                (click)="acceptBid(auction.auctionId)"
              >
                Accept Highest Bid & Close
              </button>
            </div>

          </div>
        </div>

        <app-empty-state 
          *ngIf="!loading && auctions.length === 0"
          icon="gavel"
          title="No Active Auctions Right Now"
          description="Farmers can start new live crop auctions from their Farmer Portal at any time."
        ></app-empty-state>

      </div>
    </div>
  `,
  styles: [`
    .page-title {
      font-size: 1.875rem;
      font-weight: 800;
      color: var(--dark);
    }
    .auctions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
      gap: 1.5rem;
    }
    .live-pulse {
      display: flex;
      align-items: center;
      gap: 0.35rem;
      background: #dcfce7;
      padding: 0.25rem 0.5rem;
      border-radius: var(--radius-full);
    }
    .pulse-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: #16a34a;
      box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.4);
      animation: pulse 1.5s infinite;
    }
    @keyframes pulse {
      0% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0.7); }
      70% { transform: scale(1); box-shadow: 0 0 0 6px rgba(34, 197, 94, 0); }
      100% { transform: scale(0.95); box-shadow: 0 0 0 0 rgba(34, 197, 94, 0); }
    }
    .text-2xs { font-size: 0.625rem; }
  `]
})
export class LiveAuctionsComponent implements OnInit, OnDestroy {
  private biddingService = inject(BiddingService);
  authService = inject(AuthService);
  private toast = inject(ToastService);

  loading = true;
  auctions: CropAuction[] = [];
  bidAmounts: { [key: number]: number } = {};
  showCreateModal = false;
  private wsSub?: Subscription;

  ngOnInit(): void {
    this.loadAuctions();
    this.biddingService.connectWebSocket();
    this.wsSub = this.biddingService.liveBids$.subscribe(bid => {
      this.toast.info(`New bid placed on Auction #${bid.auctionId}: ₹${bid.bidPricePerKg}/KG`);
      this.loadAuctions();
    });
  }

  ngOnDestroy(): void {
    this.wsSub?.unsubscribe();
    this.biddingService.disconnectWebSocket();
  }

  loadAuctions(): void {
    this.biddingService.getLiveAuctions().subscribe({
      next: (res) => {
        this.auctions = res || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  submitBid(auction: CropAuction): void {
    if (!this.authService.isAuthenticated()) {
      this.toast.warning('Please log in as a Dealer to place bids on auctions.');
      return;
    }

    const amount = this.bidAmounts[auction.auctionId];
    const currentPrice = auction.currentHighestBid || auction.startingPricePerKg;

    if (!amount || amount <= currentPrice) {
      this.toast.warning(`Bid must be higher than current highest price of ₹${currentPrice}/KG.`);
      return;
    }

    const dealerId = this.authService.getUserId() || 201;

    this.biddingService.placeBid({
      auctionId: auction.auctionId,
      dealerId: dealerId,
      dealerName: this.authService.currentUser()?.fullName,
      bidPricePerKg: amount
    }).subscribe({
      next: () => {
        this.toast.success(`Bid of ₹${amount}/KG placed successfully!`);
        this.loadAuctions();
      },
      error: () => {}
    });
  }

  acceptBid(auctionId: number): void {
    this.biddingService.acceptWinningBid(auctionId).subscribe({
      next: () => {
        this.toast.success('Winning bid accepted! Order generated automatically.');
        this.loadAuctions();
      },
      error: () => {}
    });
  }
}
