import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ToastService } from '../../../core/services/toast.service';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="toast-container" *ngIf="toastService.toasts().length > 0">
      <div 
        *ngFor="let t of toastService.toasts()" 
        class="toast-item"
        [ngClass]="t.type"
      >
        <span class="material-symbols-outlined icon">
          {{ getIcon(t.type) }}
        </span>
        <div class="message">{{ t.message }}</div>
        <button class="close-btn" (click)="toastService.remove(t.id)">
          <span class="material-symbols-outlined">close</span>
        </button>
      </div>
    </div>
  `,
  styles: [`
    .icon {
      font-size: 20px;
    }
    .toast-item.success .icon { color: var(--success); }
    .toast-item.error .icon { color: var(--danger); }
    .toast-item.warning .icon { color: var(--warning); }
    .toast-item.info .icon { color: var(--info); }
    .message {
      flex: 1;
      font-size: 0.875rem;
      font-weight: 500;
      color: var(--text-main);
    }
    .close-btn {
      background: none;
      border: none;
      color: var(--text-muted);
      cursor: pointer;
      display: flex;
      align-items: center;
    }
    .close-btn:hover {
      color: var(--dark);
    }
    .close-btn span {
      font-size: 16px;
    }
  `]
})
export class ToastComponent {
  toastService = inject(ToastService);

  getIcon(type: string): string {
    switch (type) {
      case 'success': return 'check_circle';
      case 'error': return 'error';
      case 'warning': return 'warning';
      default: return 'info';
    }
  }
}
