import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { FarmerReceipt, InvoiceResponse } from '../models/invoice.models';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.invoices}`;

  getInvoiceByOrderId(orderId: number): Observable<InvoiceResponse> {
    return this.http.get<InvoiceResponse>(`${this.baseUrl}/order/${orderId}`);
  }

  getFarmerReceiptByOrderId(orderId: number): Observable<FarmerReceipt> {
    return this.http.get<FarmerReceipt>(`${this.baseUrl}/farmer/order/${orderId}`);
  }

  getDealerInvoices(dealerId: number): Observable<InvoiceResponse[]> {
    return this.http.get<InvoiceResponse[]>(`${this.baseUrl}/dealer/${dealerId}`);
  }

  getFarmerInvoices(farmerId: number): Observable<InvoiceResponse[]> {
    return this.http.get<InvoiceResponse[]>(`${this.baseUrl}/farmer/${farmerId}`);
  }

  downloadInvoicePdf(orderId: number): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/order/${orderId}/download`, {
      responseType: 'blob'
    });
  }
}
