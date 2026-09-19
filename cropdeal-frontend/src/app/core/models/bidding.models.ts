export type AuctionStatus = 'LIVE' | 'ACCEPTED' | 'CLOSED' | 'EXPIRED';

export interface CropAuction {
  auctionId: number;
  farmerId: number;
  cropId: number;
  cropName: string;
  quantityKg: number;
  startingPricePerKg: number;
  reservePricePerKg?: number;
  currentHighestBid?: number;
  highestBidderId?: number;
  totalBidsCount: number;
  status: AuctionStatus;
  startTime: string;
  endTime: string;
  createdAt: string;
}

export interface AuctionCreateRequest {
  farmerId: number;
  cropId: number;
  cropName: string;
  quantityKg: number;
  startingPricePerKg: number;
  reservePricePerKg?: number;
  durationMinutes: number;
}

export interface AuctionBid {
  bidId: number;
  auctionId: number;
  dealerId: number;
  dealerName?: string;
  bidPricePerKg: number;
  totalBidAmount: number;
  placedAt: string;
}

export interface BidPlacementRequest {
  auctionId: number;
  dealerId: number;
  dealerName?: string;
  bidPricePerKg: number;
}
