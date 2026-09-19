export type NotificationType = 
  | 'ORDER_PLACED' 
  | 'ORDER_STATUS' 
  | 'DELIVERY_ASSIGNED' 
  | 'DELIVERY_ACCEPTED' 
  | 'DELIVERY_COMPLETED' 
  | 'PAYMENT_RECEIVED' 
  | 'ESCROW_RELEASED' 
  | 'ESCROW_DISPUTED' 
  | 'NEGOTIATION_OFFER' 
  | 'PRICE_ALERT' 
  | 'GENERAL';

export interface NotificationRecord {
  id: number;
  userId: number;
  title: string;
  message: string;
  type: NotificationType;
  referenceId?: string;
  isRead: boolean;
  createdAt: string;
}

export interface ChatRequest {
  message: string;
  userId?: number;
  role?: string;
  context?: string;
}

export interface ChatResponse {
  reply: string;
  suggestions?: string[];
  recommendedCrops?: any[];
  timestamp: string;
}

export interface PlatformSummaryReport {
  totalUsers: number;
  totalFarmers: number;
  totalDealers: number;
  totalDeliveryPartners: number;
  totalActiveCrops: number;
  totalOrdersCompleted: number;
  grossMarketplaceValue: number;
  activeEscrowVolume: number;
}
