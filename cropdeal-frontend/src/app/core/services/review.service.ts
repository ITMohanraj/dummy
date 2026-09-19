import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CreateReviewRequest, FarmerReputation, FarmerReview } from '../models/review.models';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.reviews}`;

  postReview(request: CreateReviewRequest): Observable<FarmerReview> {
    return this.http.post<FarmerReview>(this.baseUrl, request);
  }

  getFarmerReviews(farmerId: number): Observable<FarmerReview[]> {
    return this.http.get<FarmerReview[]>(`${this.baseUrl}/farmer/${farmerId}`);
  }

  getFarmerReputation(farmerId: number): Observable<FarmerReputation> {
    return this.http.get<FarmerReputation>(`${this.baseUrl}/reputation/farmer/${farmerId}`);
  }
}
