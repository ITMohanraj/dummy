export type EscrowHoldType = 'ORDER_PAYMENT' | 'DELIVERY_FEE';
export type EscrowHoldStatus = 'HELD' | 'RELEASED' | 'REFUNDED' | 'DISPUTED' | 'RESOLVED';

export interface UserWallet {
  walletId: number;
  userId: number;
  balance: number;
  escrowBalance: number;
  totalEarnings: number;
  totalSpent: number;
  status: 'ACTIVE' | 'FROZEN';
  updatedAt: string;
}

export interface WalletTransaction {
  transactionId: number;
  userId: number;
  amount: number;
  transactionType: 'CREDIT' | 'DEBIT' | 'ESCROW_HOLD' | 'ESCROW_RELEASE' | 'ESCROW_REFUND';
  description: string;
  referenceId?: string;
  balanceAfter: number;
  createdAt: string;
}

export interface DeliveryEscrowHold {
  id: number;
  orderId?: number;
  deliveryId?: number;
  payerId: number;
  beneficiaryId: number;
  amount: number;
  holdType: EscrowHoldType;
  status: EscrowHoldStatus;
  disputeReason?: string;
  resolutionNotes?: string;
  createdAt: string;
  resolvedAt?: string;
}

export interface DomainEvent {
  eventId: string;
  aggregateId: string;
  eventType: string;
  eventData: string;
  occurredOn: string;
  version: number;
}
