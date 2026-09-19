import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CounterOfferRequest, NegotiationCreateRequest, NegotiationResponse } from '../models/negotiation.models';

@Injectable({
  providedIn: 'root'
})
export class NegotiationService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.negotiations}`;

  createNegotiation(request: NegotiationCreateRequest): Observable<NegotiationResponse> {
    return this.http.post<NegotiationResponse>(this.baseUrl, request);
  }

  getNegotiationById(id: number): Observable<NegotiationResponse> {
    return this.http.get<NegotiationResponse>(`${this.baseUrl}/${id}`);
  }

  getCropNegotiations(cropId: number): Observable<NegotiationResponse[]> {
    return this.http.get<NegotiationResponse[]>(`${this.baseUrl}/crop/${cropId}`);
  }

  getFarmerNegotiations(farmerId: number): Observable<NegotiationResponse[]> {
    return this.http.get<NegotiationResponse[]>(`${this.baseUrl}/farmer/${farmerId}`);
  }

  getDealerNegotiations(dealerId: number): Observable<NegotiationResponse[]> {
    return this.http.get<NegotiationResponse[]>(`${this.baseUrl}/dealer/${dealerId}`);
  }

  counterOffer(negotiationId: number, request: CounterOfferRequest): Observable<NegotiationResponse> {
    return this.http.post<NegotiationResponse>(`${this.baseUrl}/${negotiationId}/counter`, request);
  }

  acceptOffer(negotiationId: number): Observable<NegotiationResponse> {
    return this.http.post<NegotiationResponse>(`${this.baseUrl}/${negotiationId}/accept`, {});
  }

  rejectOffer(negotiationId: number): Observable<NegotiationResponse> {
    return this.http.post<NegotiationResponse>(`${this.baseUrl}/${negotiationId}/reject`, {});
  }

  sendOffer(negotiationId: number, request: CounterOfferRequest): Observable<NegotiationResponse> {
    return this.counterOffer(negotiationId, request);
  }
}
