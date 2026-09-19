import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface FarmerProfileDto {
  id?: number;
  userId: number;
  fullName: string;
  email: string;
  phone: string;
  address?: string;
  state?: string;
  district?: string;
  village?: string;
  latitude?: number;
  longitude?: number;
  profileImageUrl?: string;
  bankAccountName?: string;
  bankAccountNumber?: string;
  ifscCode?: string;
  verificationStatus?: string;
  isProfileCompleted?: boolean;
}

export interface DealerProfileDto {
  id?: number;
  userId: number;
  businessName: string;
  ownerName: string;
  email: string;
  phone: string;
  address?: string;
  state?: string;
  district?: string;
  latitude?: number;
  longitude?: number;
  profileImageUrl?: string;
  bankAccountName?: string;
  bankAccountNumber?: string;
  ifscCode?: string;
  verificationStatus?: string;
  isProfileCompleted?: boolean;
}

export interface DeliveryPartnerProfileDto {
  id?: number;
  userId: number;
  fullName: string;
  email: string;
  phone: string;
  address?: string;
  state?: string;
  district?: string;
  latitude?: number;
  longitude?: number;
  profileImageUrl?: string;
  vehicleType?: string;
  vehicleNumber?: string;
  drivingLicense?: string;
  availabilityStatus?: string;
  bankAccountName?: string;
  bankAccountNumber?: string;
  ifscCode?: string;
  verificationStatus?: string;
  isProfileCompleted?: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.users}`;

  getFarmerProfile(userId: number): Observable<FarmerProfileDto> {
    return this.http.get<FarmerProfileDto>(`${this.baseUrl}/profile/farmer/${userId}`);
  }

  updateFarmerProfile(userId: number, request: Partial<FarmerProfileDto>): Observable<FarmerProfileDto> {
    return this.http.put<FarmerProfileDto>(`${this.baseUrl}/profile/farmer/${userId}`, request);
  }

  getDealerProfile(userId: number): Observable<DealerProfileDto> {
    return this.http.get<DealerProfileDto>(`${this.baseUrl}/profile/dealer/${userId}`);
  }

  updateDealerProfile(userId: number, request: Partial<DealerProfileDto>): Observable<DealerProfileDto> {
    return this.http.put<DealerProfileDto>(`${this.baseUrl}/profile/dealer/${userId}`, request);
  }

  getDeliveryPartnerProfile(userId: number): Observable<DeliveryPartnerProfileDto> {
    return this.http.get<DeliveryPartnerProfileDto>(`${this.baseUrl}/profile/delivery-partner/${userId}`);
  }

  updateDeliveryPartnerProfile(userId: number, request: Partial<DeliveryPartnerProfileDto>): Observable<DeliveryPartnerProfileDto> {
    return this.http.put<DeliveryPartnerProfileDto>(`${this.baseUrl}/profile/delivery-partner/${userId}`, request);
  }

  getUnifiedProfile(userId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/profile/${userId}`);
  }

  updateUnifiedProfile(userId: number, request: any): Observable<any> {
    return this.http.put<any>(`${this.baseUrl}/profile/${userId}`, request);
  }
}
