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
  counterPricePerKg: number;
  message?: string;
}
