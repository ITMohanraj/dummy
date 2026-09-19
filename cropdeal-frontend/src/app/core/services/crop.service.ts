import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { CropCategory, CropCreateRequest, CropResponse, Page } from '../models/crop.models';

@Injectable({
  providedIn: 'root'
})
export class CropService {
  private http = inject(HttpClient);
  private baseUrl = `${environment.apiGatewayUrl}${environment.endpoints.crops}`;

  searchCrops(params: {
    cropName?: string;
    category?: CropCategory;
    state?: string;
    district?: string;
    organic?: boolean;
    page?: number;
    size?: number;
    sort?: string;
  }): Observable<Page<CropResponse>> {
    let httpParams = new HttpParams();

    if (params.cropName) httpParams = httpParams.set('cropName', params.cropName);
    if (params.category) httpParams = httpParams.set('category', params.category);
    if (params.state) httpParams = httpParams.set('state', params.state);
    if (params.district) httpParams = httpParams.set('district', params.district);
    if (params.organic !== undefined) httpParams = httpParams.set('organic', params.organic.toString());
    if (params.page !== undefined) httpParams = httpParams.set('page', params.page.toString());
    if (params.size !== undefined) httpParams = httpParams.set('size', params.size.toString());
    if (params.sort) httpParams = httpParams.set('sort', params.sort);

    return this.http.get<Page<CropResponse>>(`${this.baseUrl}/search`, { params: httpParams });
  }

  getAllCrops(): Observable<CropResponse[]> {
    return this.http.get<CropResponse[]>(this.baseUrl);
  }

  getCropById(cropId: number): Observable<CropResponse> {
    return this.http.get<CropResponse>(`${this.baseUrl}/${cropId}`);
  }

  getFarmerCrops(farmerId: number): Observable<CropResponse[]> {
    return this.http.get<CropResponse[]>(`${this.baseUrl}/farmer/${farmerId}`);
  }

  getNearbyCrops(lat: number, lng: number, radiusKm: number = 50.0): Observable<CropResponse[]> {
    const params = new HttpParams()
      .set('latitude', lat.toString())
      .set('longitude', lng.toString())
      .set('radiusKm', radiusKm.toString());

    return this.http.get<CropResponse[]>(`${this.baseUrl}/nearby`, { params });
  }

  createCrop(request: CropCreateRequest): Observable<CropResponse> {
    return this.http.post<CropResponse>(this.baseUrl, request);
  }

  restockCrop(cropId: number, addedQuantityKg: number): Observable<CropResponse> {
    const params = new HttpParams().set('addedQuantityKg', addedQuantityKg.toString());
    return this.http.patch<CropResponse>(`${this.baseUrl}/${cropId}/restock`, null, { params });
  }
}
