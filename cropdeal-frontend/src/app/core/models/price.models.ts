export interface MandiPriceRecord {
  id: number;
  commodity: string;
  normalizedCommodity?: string;
  variety?: string;
  grade?: string;
  state: string;
  district: string;
  market: string;
  minPrice: number;
  modalPrice: number;
  maxPrice: number;
  sourceUnit: string;
  convertedPricePerKg: number;
  recordDate: string;
  syncedAt?: string;
}

export interface PriceValidationResponse {
  valid: boolean;
  commodity: string;
  referencePrice: number;
  minAllowedPrice: number;
  maxAllowedPrice: number;
  unit: string;
  message: string;
}

export interface PriceAlertRequest {
  dealerId: number;
  cropName: string;
  state?: string;
  district?: string;
  targetPricePerKg: number;
}

export interface PriceAlertResponse {
  alertId: number;
  dealerId: number;
  cropName: string;
  state?: string;
  district?: string;
  targetPricePerKg: number;
  status: 'ACTIVE' | 'TRIGGERED' | 'EXPIRED';
  createdAt: string;
}
