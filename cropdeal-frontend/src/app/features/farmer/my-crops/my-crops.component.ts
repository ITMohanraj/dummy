import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { CropService } from '../../../core/services/crop.service';
import { ToastService } from '../../../core/services/toast.service';
import { CropResponse } from '../../../core/models/crop.models';
import { SidebarComponent, NavSection } from '../../../shared/components/sidebar/sidebar.component';
import { LoadingSpinnerComponent } from '../../../shared/components/loading-spinner/loading-spinner.component';
import { EmptyStateComponent } from '../../../shared/components/empty-state/empty-state.component';
import { CurrencyInrPipe } from '../../../shared/pipes/currency-inr.pipe';

@Component({
  selector: 'app-my-crops',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, SidebarComponent, LoadingSpinnerComponent, EmptyStateComponent, CurrencyInrPipe],
  template: `
    <div class="dashboard-layout">
      <!-- Role Sidebar -->
      <app-sidebar [sections]="sidebarSections"></app-sidebar>

      <main class="dashboard-main">
        <!-- Page Header -->
        <div class="page-header flex justify-between items-center mb-6">
          <div>
            <h1 class="page-title">My Harvest Listings</h1>
            <p class="page-subtitle">Manage, edit prices, update available stock, or withdraw listings</p>
          </div>
          <a routerLink="/farmer/crops/new" class="btn btn-primary btn-sm">
            <span class="material-symbols-outlined text-sm">add_circle</span>
            List New Crop
          </a>
        </div>

        <app-loading-spinner *ngIf="loading" message="Loading your harvest inventory..."></app-loading-spinner>

        <app-empty-state 
          *ngIf="!loading && crops.length === 0"
          icon="inventory_2"
          title="No Crop Listings Yet"
          message="You haven't listed any farm produce for sale. Add your first crop with government mandi rate guidance."
          actionText="List New Crop"
          actionRoute="/farmer/crops/new"
        ></app-empty-state>

        <!-- Crops Inventory Table -->
        <div *ngIf="!loading && crops.length > 0" class="table-container card">
          <table class="custom-table">
            <thead>
              <tr>
                <th>Crop Variety</th>
                <th>Category</th>
                <th>Available Quantity</th>
                <th>Asking Price</th>
                <th>Location</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let crop of crops">
                <td>
                  <div class="flex items-center gap-3">
                    <img 
                      [src]="crop.imageUrl || 'https://images.unsplash.com/photo-1592924357228-91a4daadcfea?auto=format&fit=crop&w=120&q=80'" 
                      alt="{{ crop.cropName }}" 
                      class="crop-thumb"
                    />
                    <div>
                      <div class="font-bold text-dark text-sm">{{ crop.cropName }}</div>
                      <div class="text-2xs text-muted">ID: #CRP-{{ crop.cropId }}</div>
                    </div>
                  </div>
                </td>
                <td><span class="badge badge-green text-xs">{{ crop.category }}</span></td>
                <td class="font-bold">{{ crop.availableQuantityKg }} KG</td>
                <td class="font-bold text-primary">{{ crop.pricePerKg | inr }}/KG</td>
                <td class="text-xs text-muted">{{ crop.state }}, {{ crop.district }}</td>
                <td>
                  <span class="badge" [ngClass]="crop.status === 'AVAILABLE' ? 'badge-green' : 'badge-gold'">
                    {{ crop.status || 'AVAILABLE' }}
                  </span>
                </td>
                <td>
                  <div class="flex items-center gap-2">
                    <a [routerLink]="['/crops', crop.cropId]" class="icon-action-btn text-primary" title="View Public Listing">
                      <span class="material-symbols-outlined text-sm">visibility</span>
                    </a>
                    <button class="icon-action-btn text-emerald-700" (click)="openEditModal(crop)" title="Edit Crop">
                      <span class="material-symbols-outlined text-sm">edit</span>
                    </button>
                    <button class="icon-action-btn text-danger" (click)="deleteCrop(crop.cropId)" title="Delete Crop">
                      <span class="material-symbols-outlined text-sm">delete</span>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Interactive Edit Modal -->
        <div *ngIf="showEditModal" class="modal-overlay">
          <div class="modal-card card p-6">
            <div class="flex justify-between items-center pb-3 border-b mb-4">
              <h3 class="font-bold text-lg text-dark">Edit Crop Listing: {{ editingCrop?.cropName }}</h3>
              <button class="close-btn" (click)="closeEditModal()">
                <span class="material-symbols-outlined">close</span>
              </button>
            </div>

            <form [formGroup]="editForm" (ngSubmit)="saveCropEdit()">
              <div class="form-group mb-3">
                <label class="form-label">Crop Name</label>
                <input type="text" class="form-control" formControlName="cropName">
              </div>

              <div class="grid grid-cols-2 gap-4 mb-3">
                <div class="form-group">
                  <label class="form-label">Price per KG (₹) *</label>
                  <input type="number" class="form-control" formControlName="pricePerKg">
                </div>
                <div class="form-group">
                  <label class="form-label">Available Quantity (KG) *</label>
                  <input type="number" class="form-control" formControlName="availableQuantityKg">
                </div>
              </div>

              <div class="form-group mb-4">
                <label class="form-label">Description / Quality Details</label>
                <textarea class="form-control" rows="3" formControlName="description"></textarea>
              </div>

              <div class="flex justify-end gap-3 pt-3 border-t">
                <button type="button" class="btn btn-secondary" (click)="closeEditModal()">Cancel</button>
                <button type="submit" class="btn btn-primary" [disabled]="savingEdit">
                  {{ savingEdit ? 'Saving...' : 'Save Changes' }}
                </button>
              </div>
            </form>
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
    .crop-thumb { width: 44px; height: 44px; border-radius: var(--radius-md); object-fit: cover; }
    .text-2xs { font-size: 0.6875rem; }
    .icon-action-btn { background: none; border: none; cursor: pointer; padding: 4px; border-radius: var(--radius-sm); display: flex; align-items: center; }
    .icon-action-btn:hover { background: var(--border-light); }
    .modal-overlay {
      position: fixed; top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0, 0, 0, 0.5); backdrop-filter: blur(2px);
      display: flex; align-items: center; justify-content: center;
      z-index: 2000;
    }
    .modal-card { width: 100%; max-width: 520px; background: #ffffff; }
    .close-btn { background: none; border: none; cursor: pointer; color: var(--text-muted); }
    @media (max-width: 900px) { .dashboard-main { padding: 1.25rem; } }
  `]
})
export class MyCropsComponent implements OnInit {
  private authService = inject(AuthService);
  private cropService = inject(CropService);
  private toast = inject(ToastService);
  private fb = inject(FormBuilder);

  crops: CropResponse[] = [];
  loading = true;

  showEditModal = false;
  editingCrop?: CropResponse;
  savingEdit = false;

  editForm: FormGroup = this.fb.group({
    cropName: ['', Validators.required],
    pricePerKg: [0, [Validators.required, Validators.min(1)]],
    availableQuantityKg: [0, [Validators.required, Validators.min(1)]],
    description: ['']
  });

  sidebarSections: NavSection[] = [
    {
      title: 'Overview',
      items: [{ label: 'Dashboard', icon: 'dashboard', route: '/farmer/dashboard', exact: true }]
    },
    {
      title: 'Crops Management',
      items: [
        { label: 'My Crops', icon: 'inventory_2', route: '/farmer/crops', exact: true },
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
        { label: 'Sales Reports', icon: 'analytics', route: '/farmer/reports' }
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
    this.loadCrops();
  }

  loadCrops(): void {
    this.loading = true;
    const farmerId = this.authService.getUserId() || 101;
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

  openEditModal(crop: CropResponse): void {
    this.editingCrop = crop;
    this.editForm.patchValue({
      cropName: crop.cropName,
      pricePerKg: crop.pricePerKg,
      availableQuantityKg: crop.availableQuantityKg,
      description: crop.description
    });
    this.showEditModal = true;
  }

  closeEditModal(): void {
    this.showEditModal = false;
    this.editingCrop = undefined;
  }

  saveCropEdit(): void {
    if (this.editForm.invalid || !this.editingCrop) return;

    this.savingEdit = true;
    const val = this.editForm.value;
    const cropId = this.editingCrop.cropId;

    const idx = this.crops.findIndex(c => c.cropId === cropId);
    if (idx > -1) {
      this.crops[idx] = {
        ...this.crops[idx],
        cropName: val.cropName,
        pricePerKg: val.pricePerKg,
        availableQuantityKg: val.availableQuantityKg,
        description: val.description
      };
    }
    this.savingEdit = false;
    this.toast.success('Crop listing updated successfully!');
    this.closeEditModal();
  }

  deleteCrop(id: number): void {
    if (confirm('Are you sure you want to withdraw this crop listing?')) {
      this.crops = this.crops.filter(c => c.cropId !== id);
      this.toast.info('Crop listing withdrawn.');
    }
  }
}
