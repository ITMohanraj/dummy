export type OrderStatus = 
  | 'PENDING' 
  | 'CONFIRMED' 
  | 'PAID' 
  | 'PACKED' 
  | 'OUT_FOR_DELIVERY' 
  | 'DELIVERED' 
  | 'COMPLETED' 
  | 'CANCELLED' 
  | 'DISPUTED';

export interface PurchaseRequest {
  dealerId: number;
  farmerId: number;
  cropId: number;
  cropName?: string;
  quantityKg: number;
  pricePerKg: number;
  paymentMethod: 'WALLET' | 'UPI' | 'DEBIT_CARD' | 'CREDIT_CARD';
}

export interface OrderResponse {
  orderId: number;
  dealerId: number;
  farmerId: number;
  cropId: number;
  cropName: string;
  quantityKg: number;
  pricePerKg: number;
  totalAmount: number;
  paymentId?: string;
  deliveryId?: string;
  orderType?: string;
  status: OrderStatus;
  createdAt: string;
}
