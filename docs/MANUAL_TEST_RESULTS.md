# CropDeal Manual Testing Verification Checklist & Test Execution Results

**Date**: September 17, 2026
**Target Base URL (Gateway)**: `http://localhost:8080`
**Eureka Dashboard**: `http://localhost:8761`
**RabbitMQ Console**: `http://localhost:15672` (User: `cropdeal_user` / `cropdeal_pass`)
**Postman Collection**: `docs/CropDeal_Postman_Collection.json`

---

## 1. Infrastructure
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **INF-001** | Config Server | Start `config-server` (Port 8888) | Status = UP | Application starts and loads centralized YAML configs | **PASS** |
| **INF-002** | Eureka Server | Eureka Dashboard (`http://localhost:8761`) | All 17 microservices register dynamically | Registry actively discovers all 17 microservices | **PASS** |
| **INF-003** | RabbitMQ Broker | RabbitMQ Management Console (`15672`) | Exchanges, queues, bindings active | `auth.exchange`, `crop.exchange`, `order.exchange`, `bidding.exchange`, `delivery.exchange`, `review.exchange` configured | **PASS** |
| **INF-004** | MySQL Databases | Database-per-service isolation | 14 isolated MySQL schemas | 14 distinct schemas (`auth_db` to `chatbot_db`) with independent docker volumes | **PASS** |

---

## 2. Admin Registration / Security
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **ADM-001** | `POST /api/v1/auth/login` | `mohanraj.k1110@gmail.com` / `Admin@123` | 200 OK, JWT returned, `role=ADMIN` | JWT token issued with claims `ROLE_ADMIN` | **PASS** |
| **ADM-002** | `GET /api/v1/reports/admin/analytics` | Authorization header with `ROLE_FARMER` token | 403 Forbidden | Access denied for non-admin role | **PASS** |
| **ADM-003** | `GET /api/v1/reports/admin/analytics` | Authorization header with `ROLE_DEALER` token | 403 Forbidden | Access denied for non-admin role | **PASS** |
| **ADM-004** | `GET /api/v1/reports/admin/analytics` | Authorization header with `ROLE_DELIVERY_PARTNER` token | 403 Forbidden | Access denied for non-admin role | **PASS** |
| **ADM-005** | `POST /api/v1/auth/admin/create-admin` | Admin payload with new admin email/password | Admin created, BCrypt encoded password | New administrator saved in `auth_db` | **PASS** |
| **ADM-006** | `POST /api/v1/auth/register` | Body with `"role": "ROLE_ADMIN"` | 400/403 Rejected | Blocked: `Admin registration is not permitted via public registration API` | **PASS** |

---

## 3. Farmer Registration & Login
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **FARM-001** | `POST /api/v1/auth/register` | Name, `farmer1@gmail.com`, `Farmer@123`, `ROLE_FARMER` | 201 Created | User registered and JWT token returned | **PASS** |
| **FARM-002** | `POST /api/v1/auth/register` | Duplicate `farmer1@gmail.com` | 409 Conflict | UserAlreadyExistsException caught and handled | **PASS** |
| **FARM-003** | `POST /api/v1/auth/register` | Invalid email `farmer@` | 400 Bad Request | Jakarta Bean Validation rejects invalid format | **PASS** |
| **FARM-004** | `POST /api/v1/auth/register` | Weak password (blank or < 6 chars) | 400 Bad Request | Validation failure returned | **PASS** |
| **FARM-005** | `POST /api/v1/auth/login` | Valid Farmer credentials | 200 OK with `role=ROLE_FARMER` | JWT token returned | **PASS** |

---

## 4. Dealer Registration & Login
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **DEAL-001** | `POST /api/v1/auth/register` | Name, `dealer1@gmail.com`, `Dealer@123`, `ROLE_DEALER` | 201 Created | Dealer profile and auth record created | **PASS** |
| **DEAL-002** | `POST /api/v1/auth/login` | Valid Dealer credentials | 200 OK with `role=ROLE_DEALER` | JWT token returned | **PASS** |

---

## 5. Delivery Partner Registration & Login
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **DELREG-001**| `POST /api/v1/auth/register` | Name, `delivery1@gmail.com`, `Delivery@123`, `ROLE_DELIVERY_PARTNER` | 201 Created | Partner profile and auth record created | **PASS** |
| **DELREG-002**| `POST /api/v1/auth/login` | Valid Delivery Partner credentials | 200 OK with `role=ROLE_DELIVERY_PARTNER` | JWT token returned | **PASS** |

---

## 6. JWT Security
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **SEC-001** | Any Protected API | Request without `Authorization` header | 401 Unauthorized | Gateway / Filter rejects unauthenticated request | **PASS** |
| **SEC-002** | Any Protected API | `Authorization: Bearer abc123` | 401 Unauthorized | Malformed / invalid signature rejected | **PASS** |
| **SEC-003** | Any Protected API | Expired JWT token | 401 Unauthorized | Token expiration check enforces re-login | **PASS** |
| **SEC-004** | Dealer-Only Endpoint | Farmer token accessing Dealer endpoint | 403 Forbidden | RBAC denies cross-role authorization | **PASS** |

---

## 7. Facebook OAuth2
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **OAUTH-001**| `POST /api/v1/auth/facebook`| Valid FB access token and ID | 200 OK | OAuth credentials verified | **PASS** |
| **OAUTH-002**| `POST /api/v1/auth/facebook`| First-time Facebook user | 200 OK (New account initialized) | Auto-creates account with default role | **PASS** |
| **OAUTH-003**| `POST /api/v1/auth/facebook`| Existing Facebook user login | 200 OK (Existing account retrieved) | Authenticates existing user without duplicates | **PASS** |

---

## 8. Farmer Profile
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **PROF-001** | `POST /api/v1/users/profile` | Name, Phone, Address, State, District, Bank details | 201 Created / 200 OK | Profile persisted in `user_db` | **PASS** |
| **PROF-002** | `PUT /api/v1/users/profile/{userId}` | Updated address/bank info | 200 OK | Modified profile fields updated | **PASS** |
| **PROF-003** | `GET /api/v1/users/profile/{userId}` | Attempt to read another user private profile | 403 Forbidden | Enforces tenant/user privacy | **PASS** |

---

## 9. Create Crop & Listings
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **CROP-001** | `POST /api/v1/crops` | Tomato, 500 KG, ₹30/KG, Erode, Tamil Nadu | 201 Created, `status=ACTIVE` | Validated against mandi price engine and posted | **PASS** |
| **CROP-002** | `POST /api/v1/crops` | `quantityKg: 0` | 400 Bad Request | `@Positive` validation fails | **PASS** |
| **CROP-003** | `POST /api/v1/crops` | `quantityKg: -10` | 400 Bad Request | Negative quantity rejected | **PASS** |
| **CROP-004** | `POST /api/v1/crops` | `pricePerKg: -5` | 400 Bad Request | Negative price rejected | **PASS** |
| **CROP-005** | `POST /api/v1/crops` | Dealer token calling create crop | 403 Forbidden | Only farmers are allowed to post crops | **PASS** |
| **CROP-006** | `POST /api/v1/crops` | Delivery Partner token calling create crop | 403 Forbidden | Blocked | **PASS** |

---

## 10. Crop Search & Discovery
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **SEARCH-001**| `GET /api/v1/crops/search?cropName=Tomato` | Filter by cropName | Only Tomato listings | Correct subset returned | **PASS** |
| **SEARCH-002**| `GET /api/v1/crops/search?state=Tamil Nadu` | Filter by State | Only Tamil Nadu listings | Correct regional filtering | **PASS** |
| **SEARCH-003**| `GET /api/v1/crops/search?district=Erode` | Filter by District | Only Erode listings | Correct district filtering | **PASS** |
| **SEARCH-004**| `GET /api/v1/crops/search?organic=true` | Filter by organic flag | Only organic listings | Returns organic certified crops | **PASS** |
| **SEARCH-005**| `GET /api/v1/crops/nearby` | Latitude & Longitude + Radius | Nearby active crops in KM | Haversine distance calculator computes proximity | **PASS** |
| **SEARCH-006**| `GET /api/v1/crops/search?page=0&size=10` | Pagination query | Max 10 records per page | Spring Data Pageable pagination returned | **PASS** |

---

## 11. Government Mandi Price Validation
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **PRICE-001**| `GET /api/v1/prices/validate` | Tomato, ₹30/KG (within range) | `isValid: true` | Accepted | **PASS** |
| **PRICE-002**| `GET /api/v1/prices/validate` | Tomato, ₹5/KG (below range) | `isValid: false` | Rejected with price advisory | **PASS** |
| **PRICE-003**| `GET /api/v1/prices/validate` | Tomato, ₹250/KG (above range) | `isValid: false` | Rejected with price advisory | **PASS** |
| **PRICE-004**| `GET /api/v1/prices/validate` | Tomato, Tamil Nadu, Erode | Min, Max, Modal price | Mandi price ranges returned | **PASS** |
| **PRICE-005**| `GET /api/v1/prices/validate` | `state=Tamil Nadu` and `state=Tamilnadu` | Both match | Case & whitespace normalization logic handles alias | **PASS** |
| **PRICE-006**| `GET /api/v1/prices/validate` | Unknown unit | No fallback guesswork | Standardized on ₹/KG baseline | **PASS** |

---

## 12. Historical Prices & Trends
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **PRICE-HIST-001**| `GET /api/v1/prices/intelligence/compare?commodity=Tomato` | Request commodity prices | Market records returned | APMC Mandi rates compared | **PASS** |
| **PRICE-HIST-002**| `GET /api/v1/prices/intelligence/trend?commodity=Tomato` | 30-day trend request | Trend (UP/DOWN/STABLE), avg, modal price | Trend response returned | **PASS** |

---

## 13. Market Comparison
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **MARKET-001**| `GET /api/v1/prices/intelligence/compare?commodity=Onion` | Multi-market lookup | Mandi A, Mandi B, Mandi C rates | APMC rates array returned | **PASS** |

---

## 14. Double-Entry Wallet
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **WAL-001** | `GET /api/v1/wallets/user/{id}` | New user wallet creation | `balance = 0.00` | Fresh wallet starts at ₹0 | **PASS** |
| **WAL-002** | `POST /api/v1/wallets/user/{id}/topup` | Add ₹10,000 | `balance = 10000.00` | Balance credited, transaction logged | **PASS** |
| **WAL-003** | `POST /api/v1/wallets/user/{id}/debit` | Withdraw ₹2,000 | `balance = 8000.00` | Balance debited, DEBIT txn logged | **PASS** |
| **WAL-004** | `POST /api/v1/wallets/user/{id}/debit` | Attempt debit ₹20,000 | 400 Insufficient Balance | Rejected with Insufficient balance message | **PASS** |
| **WAL-005** | `GET /api/v1/wallets/transactions/user/{id}` | Check transaction history | Ledger of all credits, debits, held funds | Append-only transaction log returned | **PASS** |

---

## 15. Simulated Payments
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **PAY-001** | `POST /api/v1/payments/simulate` | `paymentMethod: UPI` | `status: SUCCESS` | Transaction processed | **PASS** |
| **PAY-002** | `POST /api/v1/payments/simulate` | `paymentMethod: DEBIT_CARD` | `status: SUCCESS` | Transaction processed | **PASS** |
| **PAY-003** | `POST /api/v1/payments/simulate` | `paymentMethod: CREDIT_CARD` | `status: SUCCESS` | Transaction processed | **PASS** |
| **PAY-004** | `POST /api/v1/payments/simulate` | `paymentMethod: WALLET` | `status: SUCCESS` | Transaction processed | **PASS** |
| **PAY-005** | `POST /api/v1/payments/simulate` | `amount: 0` | 400 Bad Request | Positive amount validation fails | **PASS** |
| **PAY-006** | `POST /api/v1/payments/simulate` | Duplicate idempotency key | Only 1 charge executed | Returns original transaction without re-debiting | **PASS** |

---

## 16. Direct Purchase Saga
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **ORDER-001**| `POST /api/v1/orders/purchase` | Purchase 100 KG of 500 KG crop | Order created, 100 KG reserved | Crop remaining = 400 KG, status = PARTIALLY_SOLD | **PASS** |
| **ORDER-002**| `POST /api/v1/orders/purchase` | Dealer requests 500 KG (only 400 remains) | 400 Rejected | Insufficient crop quantity exception thrown | **PASS** |
| **ORDER-003**| Payment succeeds | Saga orchestrator step | Order = CONFIRMED | Order confirmed, invoice generated | **PASS** |
| **ORDER-004**| Payment fails | Simulated payment failure | Order = CANCELLED, reserved qty released | Compensation event releases 100 KG back to crop | **PASS** |

---

## 17. Concurrency & Overselling Prevention
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **OVERSELL-001**| Concurrent Purchases | 50 KG total inventory: Dealer A (40 KG), Dealer B (20 KG) | Dealer A succeeds, Dealer B rejected | Total sold = 40 KG, never 60 KG | **PASS** |

---

## 18. Price Negotiation Engine
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **NEG-001** | `POST /api/v1/negotiations/offer` | Farmer ₹30, Dealer offers ₹25 | NEGOTIATING | Offer recorded with history | **PASS** |
| **NEG-002** | `POST /api/v1/negotiations/{id}/counter`| Farmer counters ₹28 | NEGOTIATING (Countered) | Counter offer updated | **PASS** |
| **NEG-003** | `POST /api/v1/negotiations/{id}/accept` | Dealer accepts ₹28 | ACCEPTED, triggers Order Saga | Order created at negotiated ₹28/KG | **PASS** |

---

## 19. Multiple Dealer Negotiation Isolation
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **NEG-004** | Multiple Dealer Offers | Dealer A offers ₹25, Dealer B offers ₹27 | Completely isolated negotiations | Dealer A cannot see or modify Dealer B's offer | **PASS** |

---

## 20. Timed Bidding & Auctions
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **BID-001** | `POST /api/v1/auctions` | Start ₹20, 100 KG, Start/End times | Auction created (LIVE / SCHEDULED) | Persisted in `bidding_db` | **PASS** |
| **BID-002** | `POST /api/v1/bids/place` | Bid ₹22 | Bid accepted | Current highest bid updated to ₹22 | **PASS** |
| **BID-003** | `POST /api/v1/bids/place` | Another dealer bids ₹25 | Highest bid = ₹25 | Current highest bid updated to ₹25 | **PASS** |
| **BID-004** | `POST /api/v1/bids/place` | Dealer bids ₹23 (lower than ₹25) | Rejected | Bid strictly higher than current highest required | **PASS** |
| **BID-005** | `POST /api/v1/bids/place` | Bid after `endDateTime` | Rejected | Auction expired error returned | **PASS** |
| **BID-006** | `POST /api/v1/auctions/{id}/accept` | Farmer accepts winning bid | ACCEPTED, order generated | Auction moves out of LIVE | **PASS** |

---

## 21–28. Delivery Logistics & Escrow
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **DEL-001** | Self Pickup | `deliveryOption: SELF_PICKUP` | No partner assigned, direct pickup | Order advances to fulfillment | **PASS** |
| **DEL-002** | Delivery Request | 10 KM route (`₹10/KM`) | Delivery Charge = ₹100 | Rate calculator computes ₹100 | **PASS** |
| **DEL-003** | Delivery Request | 25 KM route (`₹10/KM`) | Delivery Charge = ₹250 | Rate calculator computes ₹250 | **PASS** |
| **DEL-004** | Escrow Hold | Dealer wallet ₹1,000, Delivery ₹100 | Available = ₹900, Held = ₹100 | Delivery partner does NOT receive funds yet | **PASS** |
| **DEL-005** | Atomic Assignment | Partner A & B race to accept delivery | Partner A = ASSIGNED, Partner B = rejected | Concurrency check prevents dual assignment | **PASS** |
| **DEL-006** | Partner Cancellation | Partner A cancels | Status reverts to AVAILABLE | Other partners can now accept | **PASS** |
| **DEL-007** | Delivery Completion | Partner completes delivery | `DELIVERED`, ₹100 released to partner | Dealer held ₹100 credited to Partner wallet | **PASS** |
| **DEL-008** | Duplicate Completion | Send completion twice | Idempotent skip, partner receives ₹100 only once | No double payouts | **PASS** |

---

## 29. RabbitMQ Async Notifications
| Test ID | API / Component | Trigger Event | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **NOTIF-001**| `crop.posted` | Farmer creates crop listing | Notification queued/logged | Crop posted event dispatched | **PASS** |
| **NOTIF-002**| `order.created` | Dealer creates purchase | Farmer & Dealer notified | Notifications recorded | **PASS** |
| **NOTIF-003**| `payment.success`| Payment succeeds | Payment notification generated | Transaction receipt dispatched | **PASS** |
| **NOTIF-004**| `delivery.assigned` / `delivered` | Delivery partner workflow | Status updates dispatched to Dealer & Farmer | In-app notifications logged | **PASS** |

---

## 30. Post-Order Reviews & Reputation
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **REV-001** | `POST /api/v1/reviews` | Rating = 5, Comment = 'Great quality' | 201 Created | Review saved, reputation updated | **PASS** |
| **REV-002** | `POST /api/v1/reviews` | Rating = 0 | 400 Bad Request | `@Min(1)` validation error | **PASS** |
| **REV-003** | `POST /api/v1/reviews` | Rating = 6 | 400 Bad Request | `@Max(5)` validation error | **PASS** |
| **REV-004** | `POST /api/v1/reviews` | Review same order again | 400 Rejected | Duplicate review check blocks submission | **PASS** |

---

## 31. CQRS Reports & Dashboards
| Test ID | API / Component | Target | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **REP-001** | `GET /api/v1/reports/farmer/{id}` | Farmer Report | Total crops, sold qty, revenue, ratings | Read-model summary returned | **PASS** |
| **REP-002** | `GET /api/v1/reports/dealer/{id}` | Dealer Report | Purchases, total spending, deliveries | Read-model summary returned | **PASS** |
| **REP-003** | `GET /api/v1/reports/delivery/{id}`| Partner Report | Completed deliveries, earnings | Read-model summary returned | **PASS** |
| **REP-004** | `GET /api/v1/reports/admin/analytics` | Admin Report | Platform-wide aggregates across all users | Enterprise analytics returned | **PASS** |

---

## 32. Append-Only Audit Logging
| Test ID | API / Component | Input / Action | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **AUD-001** | `GET /api/v1/audit/logs` | Query audit trail | Actor, Action, Entity, Timestamp, Correlation ID | Audit logs returned without plaintext passwords | **PASS** |

---

## 33. Agricultural Advisory Chatbot
| Test ID | API / Component | Input Query | Expected Result | Actual Result | Status |
|---|---|---|---|---|---|
| **BOT-001** | `POST /api/v1/chatbot/ask` | 'What is tomato price in Erode?' | Real-time Mandi price advisory | Returns Mandi price guidance & links | **PASS** |
| **BOT-002** | `POST /api/v1/chatbot/ask` | 'Transfer ₹10,000 to user 2' | Blocked / Redirected | Direct financial mutations blocked safely | **PASS** |

---

## 34. API Gateway Routing
| Test ID | Gateway Route | Target Microservice | Status |
|---|---|---|---|
| **GTW-001** | `/api/v1/auth/**` | `auth-service:8081` | **PASS** |
| **GTW-002** | `/api/v1/users/**` | `user-service:8082` | **PASS** |
| **GTW-003** | `/api/v1/crops/**` | `crop-service:8083` | **PASS** |
| **GTW-004** | `/api/v1/prices/**` | `price-service:8084` | **PASS** |
| **GTW-005** | `/api/v1/orders/**`, `/negotiations/**` | `order-service:8085` | **PASS** |
| **GTW-006** | `/api/v1/payments/**` | `payment-service:8086` | **PASS** |
| **GTW-007** | `/api/v1/wallets/**` | `wallet-service:8087` | **PASS** |
| **GTW-008** | `/api/v1/bids/**`, `/auctions/**` | `bidding-service:8088` | **PASS** |
| **GTW-009** | `/api/v1/deliveries/**` | `delivery-service:8089` | **PASS** |
| **GTW-010** | `/api/v1/notifications/**` | `notification-service:8090` | **PASS** |
| **GTW-011** | `/api/v1/reviews/**` | `review-service:8091` | **PASS** |
| **GTW-012** | `/api/v1/reports/**` | `report-service:8092` | **PASS** |
| **GTW-013** | `/api/v1/audit/**` | `audit-service:8093` | **PASS** |
| **GTW-014** | `/api/v1/chatbot/**` | `chatbot-service:8094` | **PASS** |

---

## 35–40. Fault Tolerance & Complete E2E Scenario
- **Circuit Breaker (Resilience4j)**: Fallbacks configured for downstream latency or outages (**PASS**).
- **RabbitMQ Retry & Dead-lettering**: Graceful handling of broker reconnections (**PASS**).
- **Database Independence**: Microservices operate independently with isolated data stores (**PASS**).
- **Docker Compose Persistence**: MySQL named volumes retain all state upon container restarts (**PASS**).
- **End-to-End 30-Step Workflow**: Admin seeding → Farmer crop listing → Mandi validation → Dealer purchase Saga → ₹10/KM delivery escrow hold → Partner assignment → Delivery completion & wallet release → 5-Star review → CQRS report refresh (**PASS**).
