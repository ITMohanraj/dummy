import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="empty-state-card">
      <div class="icon-circle">
        <span class="material-symbols-outlined">{{ icon }}</span>
      </div>
      <h3>{{ title }}</h3>
      <p>{{ description }}</p>
      <button *ngIf="actionLabel" class="btn btn-primary btn-sm" (click)="actionClicked.emit()">
        {{ actionLabel }}
      </button>
    </div>
  `,
  styles: [`
    .empty-state-card {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      text-align: center;
      padding: 3rem 1.5rem;
      background: #ffffff;
      border: 1px dashed var(--border-strong);
      border-radius: var(--radius-lg);
      margin: 1.5rem 0;
    }
    .icon-circle {
      width: 64px;
      height: 64px;
      border-radius: 50%;
      background: var(--primary-subtle);
      color: var(--primary);
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 1rem;
    }
    .icon-circle .material-symbols-outlined {
      font-size: 32px;
    }
    h3 {
      font-size: 1.125rem;
      margin-bottom: 0.375rem;
      color: var(--dark);
    }
    p {
      color: var(--text-muted);
      font-size: 0.875rem;
      max-width: 360px;
      margin-bottom: 1.25rem;
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon: string = 'inventory_2';
  @Input() title: string = 'No records found';
  @Input() description: string = 'There are currently no items matching your criteria.';
  @Input() actionLabel?: string;
  @Output() actionClicked = new EventEmitter<void>();
}
