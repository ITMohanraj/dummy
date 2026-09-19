import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ChatRequest, ChatResponse, PlatformSummaryReport } from '../models/notification.models';

@Injectable({
  providedIn: 'root'
})
export class ChatbotService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.chatbot}`;

  askAssistant(request: ChatRequest): Observable<ChatResponse> {
    return this.http.post<ChatResponse>(`${this.baseUrl}/ask`, request);
  }
}

@Injectable({
  providedIn: 'root'
})
export class ReportService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.reports}`;

  getAdminSummary(): Observable<PlatformSummaryReport> {
    return this.http.get<PlatformSummaryReport>(`${this.baseUrl}/admin/summary`);
  }

  getFarmerSummary(farmerId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/farmer/summary?farmerId=${farmerId}`);
  }

  getDealerSummary(dealerId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/dealer/summary?dealerId=${dealerId}`);
  }

  getDeliverySummary(deliveryPartnerId: number): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/delivery/summary?deliveryPartnerId=${deliveryPartnerId}`);
  }
}
