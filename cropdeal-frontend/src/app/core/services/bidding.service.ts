import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, Subject } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuctionBid, AuctionCreateRequest, BidPlacementRequest, CropAuction } from '../models/bidding.models';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

@Injectable({
  providedIn: 'root'
})
export class BiddingService {
  private http = inject(HttpClient);
  private stompClient: Client | null = null;
  private liveBidsSubject = new Subject<AuctionBid>();

  public liveBids$ = this.liveBidsSubject.asObservable();

  createAuction(request: AuctionCreateRequest): Observable<CropAuction> {
    return this.http.post<CropAuction>(`${environment.apiGatewayUrl}${environment.endpoints.auctions}`, request);
  }

  getLiveAuctions(): Observable<CropAuction[]> {
    return this.http.get<CropAuction[]>(`${environment.apiGatewayUrl}${environment.endpoints.auctions}/live`);
  }

  acceptWinningBid(auctionId: number): Observable<CropAuction> {
    return this.http.post<CropAuction>(`${environment.apiGatewayUrl}${environment.endpoints.auctions}/${auctionId}/accept`, {});
  }

  placeBid(request: BidPlacementRequest): Observable<AuctionBid> {
    return this.http.post<AuctionBid>(`${environment.apiGatewayUrl}${environment.endpoints.bids}/place`, request);
  }

  getAuctionBids(auctionId: number): Observable<AuctionBid[]> {
    return this.http.get<AuctionBid[]>(`${environment.apiGatewayUrl}${environment.endpoints.bids}/auction/${auctionId}`);
  }

  getFarmerBids(farmerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiGatewayUrl}${environment.endpoints.bids}/farmer/${farmerId}`);
  }

  getDealerBids(dealerId: number): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiGatewayUrl}${environment.endpoints.bids}/dealer/${dealerId}`);
  }

  acceptBid(bidId: number): Observable<any> {
    return this.http.post<any>(`${environment.apiGatewayUrl}${environment.endpoints.bids}/${bidId}/accept`, {});
  }

  rejectBid(bidId: number): Observable<any> {
    return this.http.post<any>(`${environment.apiGatewayUrl}${environment.endpoints.bids}/${bidId}/reject`, {});
  }

  cancelBid(bidId: number): Observable<any> {
    return this.http.post<any>(`${environment.apiGatewayUrl}${environment.endpoints.bids}/${bidId}/cancel`, {});
  }

  // WebSocket / STOMP Real-Time Bidding Floor
  connectWebSocket(auctionId?: number): void {
    if (this.stompClient && this.stompClient.connected) return;

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(environment.wsBiddingUrl) as any,
      reconnectDelay: 5000,
      debug: (str) => {
        // console.log('[STOMP]', str);
      }
    });

    this.stompClient.onConnect = () => {
      // Global live bidding stream
      this.stompClient?.subscribe('/topic/auctions/live', message => {
        try {
          const bid: AuctionBid = JSON.parse(message.body);
          this.liveBidsSubject.next(bid);
        } catch {}
      });

      // Specific auction stream
      if (auctionId) {
        this.stompClient?.subscribe(`/topic/auctions/${auctionId}/bids`, message => {
          try {
            const bid: AuctionBid = JSON.parse(message.body);
            this.liveBidsSubject.next(bid);
          } catch {}
        });
      }
    };

    this.stompClient.activate();
  }

  disconnectWebSocket(): void {
    if (this.stompClient) {
      this.stompClient.deactivate();
      this.stompClient = null;
    }
  }
}
