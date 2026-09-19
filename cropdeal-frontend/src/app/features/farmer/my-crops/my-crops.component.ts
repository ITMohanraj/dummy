import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CropService } from '../../../core/services/crop.service';
import { AuthService } from '../../../core/services/auth.service';
import { ToastService } from '../../../core/services/toast.service';
import { CropResponse } from '../../../core/models/crop.models';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-my-crops',
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
    <div class="my-crops-page py-8">
      <div class="container">
        
        <div class="flex justify-between items-center mb-6 flex-wrap gap-4">
          <div>
            <h1 class="text-2xl font-extrabold text-dark">My Harvest Inventory</h1>
            <p class="text-xs text-muted">Manage active listings, update stock levels, and review price performance.</p>
          </div>

          <a routerLink="/farmer/crops/new" class="btn btn-primary btn-sm">
            <span class="material-symbols-outlined text-sm">add_circle</span>
            List New Crop
          </a>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading your crop listings..."></app-loading-spinner>

        <div *ngIf="!loading && crops.length > 0" class="table-container">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Crop Listing</th>
                <th>Category</th>
                <th>Total Listed</th>
                <th>Available Stock</th>
                <th>Listing Rate</th>
                <th>Location</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let c of crops">
                <td>
                  <div class="font-bold text-dark">{{ c.cropName }}</div>
                  <div class="text-xs text-muted">{{ c.variety || 'Deshi' }} • Grade {{ c.grade || 'A' }}</div>
                </td>
                <td><span class="badge badge-green text-2xs">{{ c.category }}</span></td>
                <td>{{ c.quantityKg }} KG</td>
                <td>
                  <span class="font-bold text-emerald-700">{{ c.availableQuantityKg }} KG</span>
                </td>
                <td>
                  <span class="font-bold">{{ c.pricePerKg | inr }}/KG</span>
                </td>
                <td class="text-xs">{{ c.district }}, {{ c.state }}</td>
                <td>
                  <span class="badge" [ngClass]="c.availableQuantityKg > 0 ? 'badge-green' : 'badge-red'">
                    {{ c.availableQuantityKg > 0 ? 'ACTIVE' : 'OUT_OF_STOCK' }}
                  </span>
                </td>
                <td>
                  <button class="btn btn-secondary btn-sm text-xs" (click)="openRestockModal(c)">
                    <span class="material-symbols-outlined text-xs">add</span>
                    Restock
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <app-empty-state 
          *ngIf="!loading && crops.length === 0"
          icon="inventory_2"
          title="No Crop Listings Published"
          description="Publish your first harvest listing to start receiving dealer purchases."
          actionLabel="List New Crop"
          (actionClicked)="router.navigate(['/farmer/crops/new'])"
        ></app-empty-state>

        <!-- Restock Modal -->
        <div *ngIf="restockTargetCrop" class="modal-backdrop">
          <div class="modal-card card p-6">
            <h3 class="font-bold text-lg mb-2">Restock {{ restockTargetCrop.cropName }}</h3>
            <p class="text-xs text-muted mb-4">Add harvested kilograms to your active listing.</p>
            
            <div class="form-group">
              <label class="form-label">Added Quantity (KG)</label>
              <input type="number" [(ngModel)]="restockQty" class="form-control" placeholder="e.g. 200" />
            </div>

            <div class="flex justify-end gap-3 mt-6">
              <button class="btn btn-secondary btn-sm" (click)="restockTargetCrop = undefined">Cancel</button>
              <button class="btn btn-primary btn-sm" (click)="submitRestock()">Confirm Restock</button>
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
export class MyCropsComponent implements OnInit {
  private cropService = inject(CropService);
  private authService = inject(AuthService);
  private toast = inject(ToastService);
  router = inject(Router);

  crops: CropResponse[] = [];
  loading = true;

  restockTargetCrop?: CropResponse;
  restockQty = 100;

  ngOnInit(): void {
    this.loadCrops();
  }

  loadCrops(): void {
    const farmerId = this.authService.getUserId() || 101;
    this.loading = true;
    this.cropService.getFarmerCrops(farmerId).subscribe({
      next: (res) => {
        this.crops = res || [];
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  openRestockModal(crop: CropResponse): void {
    this.restockTargetCrop = crop;
    this.restockQty = 100;
  }

  submitRestock(): void {
    if (!this.restockTargetCrop || this.restockQty <= 0) return;

    this.cropService.restockCrop(this.restockTargetCrop.cropId, this.restockQty).subscribe({
      next: () => {
        this.toast.success(`Successfully added ${this.restockQty} KG to ${this.restockTargetCrop?.cropName}!`);
        this.restockTargetCrop = undefined;
        this.loadCrops();
      },
      error: () => {}
    });
  }
}
