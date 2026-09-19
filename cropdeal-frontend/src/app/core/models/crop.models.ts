export type CropCategory = 
  | 'CEREALS' 
  | 'PULSES' 
  | 'VEGETABLES' 
  | 'FRUITS' 
  | 'OILSEEDS' 
  | 'SPICES' 
  | 'COMMERCIAL' 
  | 'OTHER';

export type QualityGrade = 'GRADE_A' | 'GRADE_B' | 'GRADE_C' | 'FAQ' | 'PREMIUM';
export type CropStatus = 'AVAILABLE' | 'RESERVED' | 'SOLD_OUT' | 'UNAVAILABLE';

export interface CropResponse {
  cropId: number;
  farmerId: number;
  cropName: string;
  category: CropCategory;
  variety?: string;
  grade?: string;
  quality?: QualityGrade;
  organic: boolean;
  description?: string;
  quantityKg: number;
  availableQuantityKg: number;
  pricePerKg: number;
  totalAmount?: number;
  referenceGovernmentPrice?: number;
  state: string;
  district: string;
  location?: string;
  latitude?: number;
  longitude?: number;
  imageUrl?: string;
  status: CropStatus;
  createdAt: string;
}

export interface CropCreateRequest {
  farmerId: number;
  cropName: string;
  category: CropCategory;
  variety?: string;
  grade?: string;
  quality?: QualityGrade;
  organic?: boolean;
  description?: string;
  quantityKg: number;
  pricePerKg: number;
  state: string;
  district: string;
  location?: string;
  latitude?: number;
  longitude?: number;
  harvestDate?: string;
  imageUrl?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
  empty: boolean;
}
