import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { DeliveryEscrowHold, DomainEvent, UserWallet, WalletTransaction } from '../models/wallet.models';

@Injectable({
  providedIn: 'root'
})
export class WalletService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.wallets}`;

  getWallet(userId: number, role?: string): Observable<UserWallet> {
    let params = new HttpParams();
    if (role) params = params.set('role', role);
    return this.http.get<UserWallet>(`${this.baseUrl}/user/${userId}`, { params });
  }

  topUp(userId: number, amount: number, paymentMethod: string = 'UPI'): Observable<UserWallet> {
    return this.http.post<UserWallet>(`${this.baseUrl}/user/${userId}/topup`, { amount, paymentMethod });
  }

  debit(userId: number, amount: number, reason: string = 'WITHDRAWAL'): Observable<UserWallet> {
    return this.http.post<UserWallet>(`${this.baseUrl}/user/${userId}/debit`, { amount, reason });
  }

  getTransactions(userId: number): Observable<WalletTransaction[]> {
    return this.http.get<WalletTransaction[]>(`${this.baseUrl}/transactions/user/${userId}`);
  }

  // --- Production Escrow Methods ---
  holdEscrow(payload: {
    orderId?: number;
    deliveryId?: number;
    payerId: number;
    beneficiaryId: number;
    amount: number;
    holdType: 'ORDER_PAYMENT' | 'DELIVERY_FEE';
  }): Observable<DeliveryEscrowHold> {
    return this.http.post<DeliveryEscrowHold>(`${this.baseUrl}/escrow/hold`, payload);
  }

  releaseEscrow(payload: { holdId?: number; deliveryId?: number; orderId?: number }): Observable<DeliveryEscrowHold> {
    return this.http.post<DeliveryEscrowHold>(`${this.baseUrl}/escrow/release`, payload);
  }

  refundEscrow(payload: { holdId: number; reason: string }): Observable<DeliveryEscrowHold> {
    return this.http.post<DeliveryEscrowHold>(`${this.baseUrl}/escrow/refund`, payload);
  }

  disputeEscrow(payload: { holdId: number; reason: string; raisedByUserId: number }): Observable<DeliveryEscrowHold> {
    return this.http.post<DeliveryEscrowHold>(`${this.baseUrl}/escrow/dispute`, payload);
  }

  resolveEscrow(payload: {
    holdId: number;
    resolution: 'RELEASE' | 'REFUND' | 'SPLIT';
    refundAmountToDealer?: number;
    releaseAmountToBeneficiary?: number;
    adminNotes: string;
  }): Observable<DeliveryEscrowHold> {
    return this.http.post<DeliveryEscrowHold>(`${this.baseUrl}/escrow/resolve`, payload);
  }

  getUserEscrows(userId: number): Observable<DeliveryEscrowHold[]> {
    return this.http.get<DeliveryEscrowHold[]>(`${this.baseUrl}/escrow/user/${userId}`);
  }

  getEventStream(userId: number): Observable<DomainEvent[]> {
    return this.http.get<DomainEvent[]>(`${this.baseUrl}/user/${userId}/events`);
  }
}
