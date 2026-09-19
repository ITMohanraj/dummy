import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { OrderResponse, OrderStatus, PurchaseRequest } from '../models/order.models';

@Injectable({
  providedIn: 'root'
})
export class OrderService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.orders}`;

  purchaseCrop(request: PurchaseRequest): Observable<OrderResponse> {
    return this.http.post<OrderResponse>(`${this.baseUrl}/purchase`, request);
  }

  getOrderById(orderId: number): Observable<OrderResponse> {
    return this.http.get<OrderResponse>(`${this.baseUrl}/${orderId}`);
  }

  getOrdersByDealer(dealerId: number): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/dealer/${dealerId}`);
  }

  getOrdersByFarmer(farmerId: number): Observable<OrderResponse[]> {
    return this.http.get<OrderResponse[]>(`${this.baseUrl}/farmer/${farmerId}`);
  }

  updateOrderStatus(orderId: number, status: OrderStatus): Observable<OrderResponse> {
    const params = new HttpParams().set('status', status);
    return this.http.patch<OrderResponse>(`${this.baseUrl}/${orderId}/status`, null, { params });
  }
}
