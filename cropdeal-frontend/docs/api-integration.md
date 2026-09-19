# CropDeal Frontend-Backend API Integration Mapping

Complete mapping between the Angular frontend services and the Spring Boot microservices backend via the Spring Cloud API Gateway (`http://localhost:8080`).

---

## 1. Authentication & Security Service (`auth-service`)

### Register User
- **Method**: `POST`
- **URL**: `/api/v1/auth/register`
- **Role**: Public
- **Request**:
  ```json
  {
    "fullName": "Ramesh Kumar",
    "email": "ramesh@gmail.com",
    "password": "Password123!",
    "phoneNumber": "9876543210",
    "role": "FARMER",
    "state": "Tamil Nadu",
    "district": "Salem"
  }
  ```
- **Response**: `AuthResponse` (`token`, `tokenType`, `userId`, `email`, `role`, `fullName`, `expiresIn`)

### Login User
- **Method**: `POST`
- **URL**: `/api/v1/auth/login`
- **Role**: Public
- **Request**:
  ```json
  {
    "email": "farmer@cropdeal.com",
    "password": "Password123!"
  }
  ```
- **Response**: `AuthResponse` with JWT Bearer token

---

## 2. Crop Marketplace Service (`crop-service`)

### Search & Filter Marketplace Crops
- **Method**: `GET`
- **URL**: `/api/v1/crops/search?cropName=&category=&state=&district=&organic=&page=0&size=12`
- **Role**: Public
- **Response**: `Page<CropResponse>`

### Get Crop Details
- **Method**: `GET`
- **URL**: `/api/v1/crops/{cropId}`
- **Role**: Public
- **Response**: `CropResponse`

### Publish Crop Listing
- **Method**: `POST`
- **URL**: `/api/v1/crops`
- **Role**: `FARMER`
- **Request**: `CropCreateRequest`

### Restock Crop Listing
- **Method**: `PATCH`
- **URL**: `/api/v1/crops/{cropId}/restock?addedQuantityKg=100`
- **Role**: `FARMER`

---

## 3. Official Mandi Price Service (`price-service`)

### Validate Farmer Crop Price Against Government Tolerance Window
- **Method**: `GET`
- **URL**: `/api/v1/prices/validate?cropName=Tomato&state=Tamil%20Nadu&district=Salem&grade=A&pricePerKg=25.00`
- **Role**: Public / `FARMER`
- **Response**: `PriceValidationResponse` (`valid`, `referencePrice`, `minAllowedPrice`, `maxAllowedPrice`, `message`)

### Get All Latest Mandi Rates
- **Method**: `GET`
- **URL**: `/api/v1/prices/mandi-rates?commodity=Tomato`
- **Role**: Public
- **Response**: `List<MandiPriceRecord>`

### Trigger Government Data Sync
- **Method**: `POST`
- **URL**: `/api/v1/prices/sync-government`
- **Role**: Public / `ADMIN`

---

## 4. Order & Purchase Saga Service (`order-service`)

### Direct Crop Purchase with Escrow Hold
- **Method**: `POST`
- **URL**: `/api/v1/orders/purchase`
- **Role**: `DEALER`
- **Request**: `PurchaseRequest` (`dealerId`, `farmerId`, `cropId`, `quantityKg`, `pricePerKg`, `paymentMethod`)
- **Response**: `OrderResponse`

### Update Order State
- **Method**: `PATCH`
- **URL**: `/api/v1/orders/{orderId}/status?status=PACKED`
- **Role**: `FARMER` / `DEALER` / `DELIVERY_PARTNER`

---

## 5. Digital Wallet & Production Escrow Service (`wallet-service`)

### Get User Wallet Balance
- **Method**: `GET`
- **URL**: `/api/v1/wallets/user/{userId}?role=FARMER`
- **Response**: `UserWallet` (`balance`, `escrowBalance`, `totalEarnings`, `totalSpent`)

### Top-Up Wallet
- **Method**: `POST`
- **URL**: `/api/v1/wallets/user/{userId}/topup`
- **Request**: `{"amount": 5000, "paymentMethod": "UPI"}`

### Dispute Escrow Hold
- **Method**: `POST`
- **URL**: `/api/v1/wallets/escrow/dispute`
- **Request**: `EscrowDisputeRequest`

### Admin Escrow Arbitration
- **Method**: `POST`
- **URL**: `/api/v1/wallets/escrow/resolve`
- **Role**: `ADMIN`
- **Request**: `EscrowResolveRequest` (`resolution`: `RELEASE` | `REFUND` | `SPLIT`)

---

## 6. Logistics & Fleet Service (`delivery-service`)

### Create Delivery Request (₹10/km)
- **Method**: `POST`
- **URL**: `/api/v1/deliveries/requests`
- **Role**: `DEALER`
- **Request**: `CreateDeliveryRequestDto`

### Available Consignments for Fleet
- **Method**: `GET`
- **URL**: `/api/v1/deliveries/partner/available`
- **Role**: `DELIVERY_PARTNER`

### Accept Delivery Trip
- **Method**: `POST`
- **URL**: `/api/v1/deliveries/partner/{deliveryId}/accept?deliveryPartnerId=301`
- **Role**: `DELIVERY_PARTNER`

### Update Delivery Status
- **Method**: `PATCH`
- **URL**: `/api/v1/deliveries/tracking/{deliveryId}/status?status=DELIVERED`
- **Role**: `DELIVERY_PARTNER`

---

## 7. Real-Time Bidding & WebSocket Service (`bidding-service`)

### Live Auctions
- **Method**: `GET`
- **URL**: `/api/v1/auctions/live`

### STOMP SockJS WebSocket Endpoint
- **URL**: `ws://localhost:8080/ws-bidding`
- **Subscription**: `/topic/auctions/live`, `/topic/auctions/{auctionId}/bids`
- **Publish**: `/app/bid.place`

---

## 8. Reviews & Notifications

### Post Farmer Review
- **Method**: `POST`
- **URL**: `/api/v1/reviews`
- **Role**: `DEALER`

### User Notifications
- **Method**: `GET`
- **URL**: `/api/v1/notifications/user/{userId}`
