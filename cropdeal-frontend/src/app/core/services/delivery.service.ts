import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateDeliveryRequestDto, DeliveryRequest, DeliveryStatus } from '../models/delivery.models';

@Injectable({
  providedIn: 'root'
})
export class DeliveryService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.deliveries}`;

  createDeliveryRequest(dto: CreateDeliveryRequestDto): Observable<DeliveryRequest> {
    return this.http.post<DeliveryRequest>(`${this.baseUrl}/requests`, dto);
  }

  getDeliveryById(deliveryId: number): Observable<DeliveryRequest> {
    return this.http.get<DeliveryRequest>(`${this.baseUrl}/requests/${deliveryId}`);
  }

  getAvailableDeliveries(): Observable<DeliveryRequest[]> {
    return this.http.get<DeliveryRequest[]>(`${this.baseUrl}/partner/available`);
  }

  acceptDelivery(deliveryId: number, deliveryPartnerId: number): Observable<DeliveryRequest> {
    const params = new HttpParams().set('deliveryPartnerId', deliveryPartnerId.toString());
    return this.http.post<DeliveryRequest>(`${this.baseUrl}/partner/${deliveryId}/accept`, null, { params });
  }

  updateDeliveryStatus(deliveryId: number, status: DeliveryStatus): Observable<DeliveryRequest> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<DeliveryRequest>(`${this.baseUrl}/tracking/${deliveryId}/status`, null, { params });
  }
}
