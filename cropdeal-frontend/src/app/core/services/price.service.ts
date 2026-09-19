import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { MandiPriceRecord, PriceAlertRequest, PriceAlertResponse, PriceValidationResponse } from '../models/price.models';

@Injectable({
  providedIn: 'root'
})
export class PriceService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.prices}`;

  validateFarmerPrice(
    cropName: string, 
    pricePerKg: number, 
    state?: string, 
    district?: string, 
    grade: string = 'A'
  ): Observable<PriceValidationResponse> {
    let params = new HttpParams()
      .set('cropName', cropName)
      .set('pricePerKg', pricePerKg.toString())
      .set('grade', grade);

    if (state) params = params.set('state', state);
    if (district) params = params.set('district', district);

    return this.http.get<PriceValidationResponse>(`${this.baseUrl}/validate`, { params });
  }

  getMandiRates(commodity?: string): Observable<MandiPriceRecord[]> {
    let params = new HttpParams();
    if (commodity && commodity.trim() !== '') {
      params = params.set('commodity', commodity.trim());
    }
    return this.http.get<MandiPriceRecord[]>(`${this.baseUrl}/mandi-rates`, { params });
  }

  syncGovernmentPrices(): Observable<{ status: string; recordsSynced: number; message: string }> {
    return this.http.post<{ status: string; recordsSynced: number; message: string }>(
      `${this.baseUrl}/sync-government`, {}
    );
  }

  createPriceAlert(request: PriceAlertRequest): Observable<PriceAlertResponse> {
    return this.http.post<PriceAlertResponse>(`${this.baseUrl}/alerts`, request);
  }

  getDealerAlerts(dealerId: number): Observable<PriceAlertResponse[]> {
    return this.http.get<PriceAlertResponse[]>(`${this.baseUrl}/alerts/dealer/${dealerId}`);
  }
}
