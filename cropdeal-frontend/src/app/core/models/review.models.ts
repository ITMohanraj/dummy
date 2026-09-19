export interface FarmerReview {
  reviewId: number;
  orderId: number;
  dealerId: number;
  farmerId: number;
  rating: number; // 1 - 5
  comment: string;
  createdAt: string;
}

export interface CreateReviewRequest {
  orderId: number;
  dealerId: number;
  farmerId: number;
  rating: number;
  comment: string;
}

export interface FarmerReputation {
  farmerId: number;
  averageRating: number;
  totalReviewsCount: number;
  badge: 'PLATINUM' | 'GOLD' | 'SILVER' | 'STANDARD';
}
