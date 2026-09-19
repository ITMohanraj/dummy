import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { NotificationRecord } from '../models/notification.models';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.notifications}`;

  getUserNotifications(userId: number): Observable<NotificationRecord[]> {
    return this.http.get<NotificationRecord[]>(`${this.baseUrl}/user/${userId}`);
  }

  getUnreadCount(userId: number): Observable<{ userId: number; unreadCount: number }> {
    return this.http.get<{ userId: number; unreadCount: number }>(`${this.baseUrl}/user/${userId}/unread-count`);
  }

  markAsRead(notificationId: number): Observable<{ notificationId: number; status: string }> {
    return this.http.patch<{ notificationId: number; status: string }>(`${this.baseUrl}/${notificationId}/read`, {});
  }

  markAllAsRead(userId: number): Observable<{ userId: number; status: string }> {
    return this.http.patch<{ userId: number; status: string }>(`${this.baseUrl}/user/${userId}/read-all`, {});
  }
}
