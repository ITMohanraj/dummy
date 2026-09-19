export type DeliveryStatus = 
  | 'PENDING' 
  | 'ASSIGNED' 
  | 'ACCEPTED' 
  | 'PICKED_UP' 
  | 'IN_TRANSIT' 
  | 'DELIVERED' 
  | 'CANCELLED';

export interface CreateDeliveryRequestDto {
  orderId: number;
  dealerId: number;
  farmerId: number;
  pickupAddress: string;
  deliveryAddress: string;
  distanceKm: number;
  weightKg: number;
  notes?: string;
}

export interface DeliveryRequest {
  deliveryId: number;
  orderId: number;
  dealerId: number;
  farmerId: number;
  deliveryPartnerId?: number;
  pickupAddress: string;
  deliveryAddress: string;
  distanceKm: number;
  weightKg: number;
  deliveryFee: number; // Calculated at ₹10/km
  status: DeliveryStatus;
  pickupOtp?: string;
  deliveryOtp?: string;
  notes?: string;
  createdAt: string;
  updatedAt?: string;
}

export interface DeliveryRecord {
  id: number;
  orderId: number;
  pickupLocation: string;
  dropLocation: string;
  cropName: string;
  quantity: number;
  deliveryFee: number;
  status: string;
  createdAt: string;
  deliveredAt?: string;
  recipientName?: string;
  recipientPhone?: string;
  completedAt?: string;
  currentLocation?: string;
}

