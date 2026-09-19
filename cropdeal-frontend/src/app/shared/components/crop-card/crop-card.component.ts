import { Component, Input, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { CropResponse } from '../../../core/models/crop.models';
import { AuthService } from '../../../core/services/auth.service';
import { CurrencyInrPipe } from '../../pipes/currency-inr.pipe';

@Component({
  selector: 'app-crop-card',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyInrPipe],
  template: `
    <div class="crop-card card">
      
      <!-- Image Header & Badges -->
      <div class="card-image-wrapper">
        <img 
          [src]="crop.imageUrl || getDefaultImage(crop.category)" 
          [alt]="crop.cropName"
          class="crop-image"
          loading="lazy"
        />
        
        <!-- Category & Quality Badges -->
        <div class="badge-overlay">
          <span class="badge badge-green">{{ crop.category }}</span>
          <span *ngIf="crop.organic" class="badge badge-gold">
            <span class="material-symbols-outlined text-xs">eco</span> Organic
          </span>
          <span *ngIf="crop.grade" class="badge badge-blue">Grade {{ crop.grade }}</span>
        </div>

        <!-- Available Stock Chip -->
        <div class="stock-chip" [class.low-stock]="crop.availableQuantityKg < 100">
          <span class="material-symbols-outlined text-xs">inventory_2</span>
          <span>{{ crop.availableQuantityKg }} KG available</span>
        </div>
      </div>

      <!-- Card Body -->
      <div class="card-body">
        
        <!-- Location & Farmer Origin -->
        <div class="crop-location flex items-center gap-1">
          <span class="material-symbols-outlined text-xs text-muted">location_on</span>
          <span>{{ crop.district || 'Erode' }}, {{ crop.state || 'Tamil Nadu' }}</span>
        </div>

        <!-- Crop Title & Variety -->
        <h3 class="crop-title">
          <a [routerLink]="['/crops', crop.cropId]">{{ crop.cropName }}</a>
        </h3>
        <p *ngIf="crop.variety" class="crop-variety">Variety: {{ crop.variety }}</p>

        <!-- Price Section -->
        <div class="price-box flex justify-between items-end mt-3">
          <div>
            <span class="price-label">Farm Gate Price</span>
            <div class="price-val">{{ crop.pricePerKg | inr }}<span class="unit"> / KG</span></div>
            <div class="quintal-sub">≈ {{ (crop.pricePerKg * 100) | inr }} / Quintal</div>
          </div>

          <!-- Government Mandi Reference Indicator -->
          <div *ngIf="crop.referenceGovernmentPrice" class="gov-ref-tag" title="Government Mandi Reference Price">
            <span class="text-2xs font-semibold text-emerald-800">Gov APMC:</span>
            <span class="text-xs font-bold text-emerald-700">{{ crop.referenceGovernmentPrice | inr }}/KG</span>
          </div>
        </div>

        <!-- Action Buttons: Role-Aware & Guest Friendly -->
        <div class="card-actions flex gap-2 mt-4">
          <ng-container *ngIf="authService.isAuthenticated()">
            <a [routerLink]="['/crops', crop.cropId]" class="btn btn-primary btn-sm flex-1">
              <span class="material-symbols-outlined text-sm">shopping_cart</span>
              Buy Direct
            </a>
            <a [routerLink]="['/crops', crop.cropId]" [queryParams]="{tab: 'negotiate'}" class="btn btn-secondary btn-sm" title="Negotiate Counter Offer">
              <span class="material-symbols-outlined text-sm">handshake</span>
            </a>
          </ng-container>

          <ng-container *ngIf="!authService.isAuthenticated()">
            <a [routerLink]="['/crops', crop.cropId]" class="btn btn-secondary btn-sm w-full text-center">
              <span class="material-symbols-outlined text-sm">visibility</span>
              View Details
            </a>
          </ng-container>
        </div>

      </div>

    </div>
  `,
  styles: [`
    .crop-card {
      display: flex;
      flex-direction: column;
      height: 100%;
      border-radius: var(--radius-lg);
      transition: transform 0.2s ease, box-shadow 0.2s ease;
      background: #ffffff;
      border: 1px solid var(--border-light);
    }
    .crop-card:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-lg);
    }
    .card-image-wrapper {
      position: relative;
      width: 100%;
      height: 190px;
      background: #e2e8f0;
      overflow: hidden;
    }
    .crop-image {
      width: 100%;
      height: 100%;
      object-fit: cover;
      transition: transform 0.3s ease;
    }
    .crop-card:hover .crop-image {
      transform: scale(1.05);
    }
    .badge-overlay {
      position: absolute;
      top: 0.75rem;
      left: 0.75rem;
      display: flex;
      flex-wrap: wrap;
      gap: 0.35rem;
      z-index: 2;
    }
    .stock-chip {
      position: absolute;
      bottom: 0.75rem;
      right: 0.75rem;
      background: rgba(15, 23, 42, 0.75);
      backdrop-filter: blur(4px);
      color: #ffffff;
      font-size: 0.75rem;
      font-weight: 600;
      padding: 0.25rem 0.5rem;
      border-radius: var(--radius-md);
      display: flex;
      align-items: center;
      gap: 0.25rem;
    }
    .stock-chip.low-stock {
      background: rgba(220, 38, 38, 0.85);
    }
    .card-body {
      padding: 1.25rem;
      display: flex;
      flex-direction: column;
      flex: 1;
    }
    .crop-location {
      font-size: 0.75rem;
      color: var(--text-muted);
      font-weight: 600;
      margin-bottom: 0.25rem;
    }
    .crop-title {
      font-size: 1.125rem;
      font-weight: 700;
      margin-bottom: 0.15rem;
    }
    .crop-title a {
      color: var(--dark);
    }
    .crop-title a:hover {
      color: var(--primary);
    }
    .crop-variety {
      font-size: 0.8125rem;
      color: var(--text-muted);
    }
    .price-box {
      background: #f8fafc;
      padding: 0.75rem;
      border-radius: var(--radius-md);
      border: 1px solid var(--border-light);
    }
    .price-label {
      display: block;
      font-size: 0.6875rem;
      font-weight: 700;
      color: var(--text-muted);
      text-transform: uppercase;
      letter-spacing: 0.05em;
    }
    .price-val {
      font-size: 1.25rem;
      font-weight: 800;
      color: var(--primary-dark);
      line-height: 1.1;
    }
    .unit {
      font-size: 0.75rem;
      font-weight: 600;
      color: var(--text-muted);
    }
    .quintal-sub {
      font-size: 0.6875rem;
      color: var(--text-muted);
      margin-top: 2px;
    }
    .gov-ref-tag {
      background: #dcfce7;
      border: 1px solid #bbf7d0;
      padding: 0.25rem 0.5rem;
      border-radius: var(--radius-md);
      text-align: right;
    }
    .text-2xs { font-size: 0.625rem; display: block; }
  `]
})
export class CropCardComponent {
  @Input({ required: true }) crop!: CropResponse;
  authService = inject(AuthService);

  getDefaultImage(category?: string): string {
    switch (category) {
      case 'VEGETABLES':
        return 'https://images.unsplash.com/photo-1597362925123-77861d3fbac7?auto=format&fit=crop&w=600&q=80';
      case 'FRUITS':
        return 'https://images.unsplash.com/photo-1619566636858-adf3ef46400b?auto=format&fit=crop&w=600&q=80';
      case 'CEREALS':
        return 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?auto=format&fit=crop&w=600&q=80';
      case 'PULSES':
        return 'https://images.unsplash.com/photo-1586201375761-83865001e31c?auto=format&fit=crop&w=600&q=80';
      case 'SPICES':
        return 'https://images.unsplash.com/photo-1596040033229-a9821ebd058d?auto=format&fit=crop&w=600&q=80';
      default:
        return 'https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=600&q=80';
    }
  }
}
