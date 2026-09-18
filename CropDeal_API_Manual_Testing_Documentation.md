# CropDeal Microservices Platform - Comprehensive API & Manual Testing Manual

## 1. Project Overview & Architecture
CropDeal is an agricultural direct marketplace platform architected into 17 Spring Boot microservices with Spring Cloud API Gateway, Eureka Service Discovery, RabbitMQ asynchronous event bus, and dedicated MySQL database per service.

### Base URLs:
- **API Gateway (Unified Entrance)**: `http://localhost:8080`
- **Gateway Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Eureka Dashboard**: `http://localhost:8761`

---

## 2. Complete End-to-End Business Workflow Order

1. **User Registration & Authentication (`auth-service`)**:
   - Register Farmer, Dealer, and Delivery Partner accounts.
   - Login to receive JWT Bearer token.
2. **User Profile Setup & Bank Details (`user-service`)**:
   - Fill in KYC, addresses, location coordinates, vehicle information, and bank account details.
   - Query user directory and role listings.
3. **Price Intelligence & MSP Benchmarking (`price-service`)**:
   - Query government MSP reference rates and validate pricing bands.
4. **Crop Listing & Marketplace Discovery (`crop-service`)**:
   - Farmer publishes crop listings with category, grade, location, and price.
   - Search marketplace or query nearby crops by geolocation radius.
5. **Multi-Dealer Independent Price Negotiation (`negotiation-service`)**:
   - Dealer submits counter-offers; Farmer accepts, rejects, or counter-offers.
6. **Live Bidding & Auctions (`bidding-service`)**:
   - Farmer schedules live crop auctions; Dealers place competitive bids.
7. **Purchase Saga & Distributed Transactions (`order-service`)**:
   - Dealer buys direct or concludes accepted negotiation; Saga orchestrates atomic inventory lock.
8. **Payment Processing (`payment-service`)**:
   - Idempotency-backed payments via UPI, Cards, Net Banking, or Wallet.
9. **Digital Wallet (`wallet-service`)**:
   - Real-time balance queries, escrow holds, and instant top-ups.
10. **Invoicing & PDF Receipts (`invoice-service`)**:
    - Automatic GST tax invoices for dealers and farmer payment receipts.
11. **Delivery Logistics & Fleet Tracking (`delivery-service`)**:
    - Automated ₹10/km rate estimation, delivery partner dispatch, and real-time state tracking.
12. **Real-Time Alerts (`notification-service`)**:
    - In-app notification badges and Gmail SMTP event dispatch.
13. **Farmer & Dealer Reputation (`review-service`)**:
    - 1-to-5 star ratings and reviews upon delivery completion.
14. **Analytics & Auditing (`report-service` & `audit-service`)**:
    - High-level platform analytics, farmer revenue models, and append-only audit trail.
15. **AI Agricultural Advisor (`chatbot-service`)**:
    - Real-time Q&A on crop diagnostics, pest management, and marketplace assistance.

---

## 3. Detailed Services, Controllers, Endpoints, Inputs & Outputs

---

### SERVICE 1: `auth-service` (Port: 8081 / Gateway Route: `/api/v1/auth/**`)

#### Controller: `AuthController` & `AdminAuthController`

##### Endpoint 1.1: Register User
- **Method / Path**: `POST /api/v1/auth/register`
- **Request Body (JSON)**:
```json
{
  "email": "farmer.ramesh@cropdeal.com",
  "password": "Password@123",
  "role": "ROLE_FARMER",
  "fullName": "Ramesh Kumar",
  "phone": "9876543210"
}
```
- **Response (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 27,
  "email": "farmer.ramesh@cropdeal.com",
  "role": "ROLE_FARMER",
  "fullName": "Ramesh Kumar",
  "expiresIn": 86400000
}
```

##### Endpoint 1.2: User Login
- **Method / Path**: `POST /api/v1/auth/login`
- **Request Body (JSON)**:
```json
{
  "email": "farmer.ramesh@cropdeal.com",
  "password": "Password@123"
}
```
- **Response (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "userId": 27,
  "email": "farmer.ramesh@cropdeal.com",
  "role": "ROLE_FARMER",
  "fullName": "Ramesh Kumar",
  "expiresIn": 86400000
}
```

##### Endpoint 1.3: Forgot Password (Request OTP)
- **Method / Path**: `POST /api/v1/auth/forgot-password`
- **Request Body (JSON)**:
```json
{
  "email": "farmer.ramesh@cropdeal.com"
}
```
- **Response (200 OK)**:
```json
{
  "message": "If an account exists for this email, a password reset OTP has been sent."
}
```

##### Endpoint 1.4: Verify OTP
- **Method / Path**: `POST /api/v1/auth/verify-otp`
- **Request Body (JSON)**:
```json
{
  "email": "farmer.ramesh@cropdeal.com",
  "otp": "534149"
}
```
- **Response (200 OK)**:
```json
{
  "message": "OTP verified successfully",
  "resetToken": "a1b2c3d4e5f67890abcdef1234567890"
}
```

##### Endpoint 1.5: Reset Password
- **Method / Path**: `POST /api/v1/auth/reset-password`
- **Request Body (JSON)**:
```json
{
  "resetToken": "a1b2c3d4e5f67890abcdef1234567890",
  "newPassword": "NewPassword@123",
  "confirmPassword": "NewPassword@123"
}
```
- **Response (200 OK)**:
```json
{
  "message": "Password reset successfully"
}
```

---

### SERVICE 2: `user-service` (Port: 8082 / Gateway Route: `/api/v1/users/**`)

#### Controllers: `UserController`, `UserProfileController`, `AdminUserController`

##### Endpoint 2.1: Get All Users
- **Method / Path**: `GET /api/v1/users` (Supports `?role=FARMER | DEALER | DELIVERY_PARTNER`)
- **Response (200 OK)**:
```json
{
  "totalUsers": 29,
  "totalFarmers": 14,
  "totalDealers": 12,
  "totalDeliveryPartners": 3,
  "farmers": [ ... ],
  "dealers": [ ... ],
  "deliveryPartners": [ ... ]
}
```

##### Endpoint 2.2: Get All Farmers
- **Method / Path**: `GET /api/v1/users/farmers`
- **Response (200 OK)**:
```json
[
  {
    "id": 1,
    "userId": 17,
    "fullName": "Sachin Kamaraj",
    "email": "sachinkamaraj1803@gmail.com",
    "maskedBankAccount": "XXXXXX7890",
    "ifscCode": "SBIN0001234",
    "verificationStatus": "PROFILE_VERIFIED"
  }
]
```

##### Endpoint 2.3: Update Farmer Profile
- **Method / Path**: `PUT /api/v1/users/profile/farmer/{userId}`
- **Request Body (JSON)**:
```json
{
  "fullName": "Ramesh Kumar Farmer",
  "email": "farmer.ramesh@cropdeal.com",
  "phone": "9876500001",
  "address": "12 Greenfield Farm, Gobichettipalayam",
  "state": "Tamil Nadu",
  "district": "Erode",
  "village": "Kavindapadi",
  "bankAccountName": "Ramesh Kumar",
  "bankAccountNumber": "1122334455667788",
  "ifscCode": "SBIN0001234"
}
```
- **Response (200 OK)**:
```json
{
  "id": 11,
  "userId": 27,
  "fullName": "Ramesh Kumar Farmer",
  "maskedBankAccount": "XXXXXX7788",
  "ifscCode": "SBIN0001234",
  "verificationStatus": "UNVERIFIED",
  "accountStatus": "ACTIVE"
}
```

##### Endpoint 2.4: Update Delivery Partner Profile
- **Method / Path**: `PUT /api/v1/users/profile/delivery-partner/{userId}`
- **Request Body (JSON)**:
```json
{
  "fullName": "Murugan Transporter",
  "email": "driver@cropdeal.com",
  "phone": "9876500003",
  "address": "78 Transport Nagar",
  "state": "Tamil Nadu",
  "district": "Salem",
  "vehicleType": "EICHER_TRUCK",
  "vehicleNumber": "TN30AB9900",
  "drivingLicense": "DL-TN30-2022-8877",
  "availabilityStatus": "AVAILABLE",
  "bankAccountName": "Murugan Logistics",
  "bankAccountNumber": "5544332211009988",
  "ifscCode": "ICIC0005566"
}
```
- **Response (200 OK)**:
```json
{
  "id": 3,
  "userId": 29,
  "fullName": "Murugan Transporter",
  "vehicleType": "EICHER_TRUCK",
  "vehicleNumber": "TN30AB9900",
  "maskedBankAccount": "XXXXXX9988",
  "ifscCode": "ICIC0005566"
}
```

---

### SERVICE 3: `crop-service` (Port: 8083 / Gateway Route: `/api/v1/crops/**`)

#### Controllers: `CropController`, `MarketplaceController`, `CropInventoryController`

##### Endpoint 3.1: Get All Crops
- **Method / Path**: `GET /api/v1/crops`
- **Response (200 OK)**:
```json
[
  {
    "cropId": 8,
    "farmerId": 27,
    "cropName": "Tomato",
    "category": "VEGETABLE",
    "variety": "Hybrid Tomato",
    "grade": "A",
    "quality": "PREMIUM",
    "organic": true,
    "quantityKg": 1000.0,
    "availableQuantityKg": 1000.0,
    "pricePerKg": 25.0,
    "totalAmount": 25000.0,
    "state": "Tamil Nadu",
    "district": "Erode",
    "status": "ACTIVE"
  }
]
```

##### Endpoint 3.2: Create Crop Listing
- **Method / Path**: `POST /api/v1/crops`
- **Request Body (JSON)**:
```json
{
  "farmerId": 27,
  "cropName": "Tomato",
  "category": "VEGETABLE",
  "variety": "Hybrid Tomato",
  "grade": "A",
  "quality": "PREMIUM",
  "organic": true,
  "description": "Fresh organically grown hybrid tomatoes.",
  "quantityKg": 1000.0,
  "pricePerKg": 25.0,
  "state": "Tamil Nadu",
  "district": "Erode",
  "location": "Gobichettipalayam",
  "latitude": 11.4548,
  "longitude": 77.4422,
  "imageUrl": "https://images.unsplash.com/photo-tomato.jpg"
}
```
- **Response (201 Created)**:
```json
{
  "cropId": 8,
  "farmerId": 27,
  "cropName": "Tomato",
  "availableQuantityKg": 1000.0,
  "pricePerKg": 25.0,
  "status": "ACTIVE"
}
```

---

### SERVICE 4: `price-service` (Port: 8084 / Gateway Route: `/api/v1/prices/**`)

#### Controllers: `PriceValidationController`, `PriceIntelligenceController`

##### Endpoint 4.1: Validate Crop Price
- **Method / Path**: `GET /api/v1/prices/validate?cropName=Tomato&state=Tamil%20Nadu&district=Erode&grade=A&pricePerKg=25`
- **Response (200 OK)**:
```json
{
  "cropName": "Tomato",
  "state": "Tamil Nadu",
  "district": "Erode",
  "submittedPrice": 25.0,
  "referencePrice": 24.0,
  "valid": true,
  "message": "Price is within fair market range"
}
```

---

### SERVICE 5: `negotiation-service` (Port: 8095 / Gateway Route: `/api/v1/negotiations/**`)

#### Controller: `NegotiationController`

##### Endpoint 5.1: Create Price Negotiation Offer
- **Method / Path**: `POST /api/v1/negotiations`
- **Request Body (JSON)**:
```json
{
  "cropListingId": 8,
  "dealerId": 28,
  "farmerId": 27,
  "quantityKg": 300.0,
  "offeredPricePerKg": 23.0
}
```
- **Response (201 Created)**:
```json
{
  "id": 6,
  "cropListingId": 8,
  "dealerId": 28,
  "farmerId": 27,
  "quantityKg": 300.0,
  "offeredPricePerKg": 23.0,
  "status": "NEGOTIATING"
}
```

##### Endpoint 5.2: Accept Negotiation Offer
- **Method / Path**: `POST /api/v1/negotiations/{negotiationId}/accept`
- **Response (200 OK)**:
```json
{
  "id": 6,
  "status": "ACCEPTED"
}
```

---

### SERVICE 6: `bidding-service` (Port: 8088 / Gateway Route: `/api/v1/bids/**`, `/api/v1/auctions/**`)

#### Controllers: `AuctionController`, `BidController`

##### Endpoint 6.1: Create Auction
- **Method / Path**: `POST /api/v1/auctions`
- **Request Body (JSON)**:
```json
{
  "farmerId": 27,
  "cropId": 8,
  "cropName": "Tomato",
  "quantityKg": 200.0,
  "startingPricePerKg": 22.0,
  "minimumAcceptablePrice": 20.0,
  "startDateTime": "2026-09-18T12:00:00",
  "endDateTime": "2026-09-20T18:00:00"
}
```
- **Response (201 Created)**:
```json
{
  "id": 11,
  "farmerId": 27,
  "cropName": "Tomato",
  "startingPricePerKg": 22.0,
  "status": "LIVE"
}
```

##### Endpoint 6.2: Place Live Bid
- **Method / Path**: `POST /api/v1/bids/place`
- **Request Body (JSON)**:
```json
{
  "auctionId": 11,
  "dealerId": 28,
  "bidPricePerKg": 28.5
}
```
- **Response (201 Created)**:
```json
{
  "id": 4,
  "auctionId": 11,
  "dealerId": 28,
  "bidPricePerKg": 28.5,
  "totalBidAmount": 5700.0,
  "status": "VALID"
}
```

---

### SERVICE 7: `order-service` (Port: 8085 / Gateway Route: `/api/v1/orders/**`)

#### Controller: `OrderController`

##### Endpoint 7.1: Execute Direct Purchase Saga
- **Method / Path**: `POST /api/v1/orders/purchase`
- **Request Body (JSON)**:
```json
{
  "dealerId": 28,
  "farmerId": 27,
  "cropId": 8,
  "cropName": "Tomato",
  "quantityKg": 100.0,
  "pricePerKg": 25.0
}
```
- **Response (200 OK)**:
```json
{
  "orderId": 14,
  "dealerId": 28,
  "farmerId": 27,
  "cropId": 8,
  "quantityKg": 100.0,
  "pricePerKg": 25.0,
  "totalAmount": 2500.0,
  "paymentId": "TXN_B2C789A1",
  "status": "CONFIRMED"
}
```

---

### SERVICE 8: `payment-service` (Port: 8086 / Gateway Route: `/api/v1/payments/**`)

#### Controller: `PaymentController`

##### Endpoint 8.1: Simulate Payment Transaction
- **Method / Path**: `POST /api/v1/payments/simulate`
- **Request Body (JSON)**:
```json
{
  "idempotencyKey": "PAY_ORD_14",
  "orderId": 14,
  "payerUserId": 28,
  "amount": 2500.0,
  "paymentMethod": "UPI"
}
```
- **Response (201 Created)**:
```json
{
  "transactionId": "TXN_SIM_14",
  "orderId": 14,
  "status": "SUCCESS"
}
```

---

### SERVICE 9: `wallet-service` (Port: 8087 / Gateway Route: `/api/v1/wallets/**`)

#### Controller: `WalletController`

##### Endpoint 9.1: Top-up Digital Wallet
- **Method / Path**: `POST /api/v1/wallets/user/{userId}/topup`
- **Request Body (JSON)**:
```json
{
  "amount": 25000.0,
  "source": "NET_BANKING",
  "reference": "BANK_REF_998877"
}
```
- **Response (200 OK)**:
```json
{
  "walletId": 4,
  "userId": 28,
  "balance": 25000.0
}
```

---

### SERVICE 10: `invoice-service` (Port: 8096 / Gateway Route: `/api/v1/invoices/**`)

#### Controller: `InvoiceController`

##### Endpoint 10.1: Get Invoice by Order ID
- **Method / Path**: `GET /api/v1/invoices/order/{orderId}`
- **Response (200 OK)**:
```json
{
  "id": 13,
  "invoiceNumber": "INV-14-90879",
  "orderId": 14,
  "dealerId": 28,
  "farmerId": 27,
  "totalAmount": 2500.00
}
```

---

### SERVICE 11: `delivery-service` (Port: 8089 / Gateway Route: `/api/v1/deliveries/**`)

#### Controllers: `DeliveryRequestController`, `DeliveryPartnerController`, `DeliveryTrackingController`

##### Endpoint 11.1: Create Delivery Booking
- **Method / Path**: `POST /api/v1/deliveries/requests`
- **Request Body (JSON)**:
```json
{
  "orderId": 14,
  "dealerId": 28,
  "farmerId": 27,
  "pickupLocation": "Gobichettipalayam, Erode, TN",
  "dropLocation": "45 APMC Market Yard, Coimbatore, TN",
  "distanceKm": 85.0
}
```
- **Response (201 Created)**:
```json
{
  "id": 7,
  "orderId": 14,
  "status": "AVAILABLE"
}
```

##### Endpoint 11.2: Delivery Partner Accepts Delivery
- **Method / Path**: `POST /api/v1/deliveries/partner/{deliveryId}/accept?deliveryPartnerId={partnerId}`
- **Response (200 OK)**:
```json
{
  "id": 7,
  "deliveryPartnerId": 29,
  "status": "ASSIGNED"
}
```

##### Endpoint 11.3: Update Tracking State
- **Method / Path**: `PATCH /api/v1/deliveries/tracking/{deliveryId}/status?status=IN_TRANSIT`
- **Response (200 OK)**:
```json
{
  "id": 7,
  "status": "IN_TRANSIT"
}
```

---

### SERVICE 12: `notification-service` (Port: 8090 / Gateway Route: `/api/v1/notifications/**`)

#### Controller: `NotificationController`

##### Endpoint 12.1: Get User Notifications
- **Method / Path**: `GET /api/v1/notifications/user/{userId}`
- **Response (200 OK)**:
```json
[
  {
    "id": 52,
    "userId": 27,
    "title": "Order Placed!",
    "message": "Order #14 confirmed for 100.0 KG Tomato",
    "read": false
  }
]
```

---

### SERVICE 13: `review-service` (Port: 8091 / Gateway Route: `/api/v1/reviews/**`)

#### Controllers: `ReviewController`, `FarmerReputationController`

##### Endpoint 13.1: Post Review
- **Method / Path**: `POST /api/v1/reviews`
- **Request Body (JSON)**:
```json
{
  "orderId": 14,
  "dealerId": 28,
  "farmerId": 27,
  "rating": 5,
  "comment": "Excellent quality organic tomatoes!"
}
```
- **Response (201 Created)**:
```json
{
  "id": 6,
  "orderId": 14,
  "rating": 5,
  "comment": "Excellent quality organic tomatoes!"
}
```

---

### SERVICE 14: `report-service` & `audit-service` (Ports: 8092, 8093)

##### Endpoint 14.1: Admin Platform Report
- **Method / Path**: `GET /api/v1/reports/admin/summary`
- **Response (200 OK)**:
```json
{
  "totalSalesCount": 14,
  "totalRevenue": 47500.0
}
```

##### Endpoint 14.2: Get Audit Trail Logs
- **Method / Path**: `GET /api/v1/audit/logs`
- **Response (200 OK)**:
```json
[
  {
    "id": 97,
    "serviceName": "order-service",
    "action": "ORDER_CONFIRMED",
    "details": "Order #14 placed"
  }
]
```

---

### SERVICE 15: `chatbot-service` (Port: 8094 / Gateway Route: `/api/v1/chatbot/**`)

#### Controller: `ChatbotController`

##### Endpoint 15.1: Ask Advisory Question
- **Method / Path**: `POST /api/v1/chatbot/ask`
- **Request Body (JSON)**:
```json
{
  "userId": 27,
  "role": "FARMER",
  "message": "What is the recommended fertilizer schedule for hybrid tomatoes in Tamil Nadu?"
}
```
- **Response (200 OK)**:
```json
{
  "query": "What is the recommended fertilizer schedule for hybrid tomatoes in Tamil Nadu?",
  "answer": "Welcome to CropDeal! I can help you with crop prices, crop listings, negotiations, auctions, delivery logistics, and wallet inquiries.",
  "intent": "GENERAL_QUERY"
}
```