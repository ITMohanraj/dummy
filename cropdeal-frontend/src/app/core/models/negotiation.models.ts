export type NegotiationStatus = 'PENDING' | 'COUNTERED' | 'ACCEPTED' | 'REJECTED' | 'EXPIRED';

export interface NegotiationResponse {
  negotiationId: number;
  cropId: number;
  cropName: string;
  dealerId: number;
  farmerId: number;
  offeredPricePerKg: number;
  requestedQuantityKg: number;
  status: NegotiationStatus;
  lastOfferBy: 'DEALER' | 'FARMER';
  counterPricePerKg?: number;
  message?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface NegotiationCreateRequest {
  cropId: number;
  dealerId: number;
  farmerId: number;
  offeredPricePerKg: number;
  requestedQuantityKg: number;
  message?: string;
}

export interface CounterOfferRequest {
  counterPricePerKg?: number;
  offeredPrice?: number;
  message?: string;
}

export interface NegotiationMessage {
  id?: number;
  sender?: 'DEALER' | 'FARMER';
  senderRole?: 'DEALER' | 'FARMER';
  senderName?: string;
  offeredPrice?: number;
  text?: string;
  note?: string;
  time?: string;
  timestamp?: string;
}

export interface NegotiationRecord {
  id: number;
  cropId: number;
  cropName: string;
  dealerId?: number;
  farmerId?: number;
  farmerName?: string;
  dealerName?: string;
  partyName?: string;
  quantity?: number;
  currentPrice?: number;
  proposedPrice: number;
  agreedPrice?: number;
  status: string;
  time?: string;
  updatedAt?: string;
  messages: NegotiationMessage[];
}

