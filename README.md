# CropDeal – Agricultural Direct Marketplace & Intelligence Platform Backend

CropDeal is a distributed, production-grade agricultural marketplace backend built strictly following **Microservices Architecture**, **Database-per-Service**, **Domain-Driven Event Architecture**, **Saga Pattern**, **CQRS Read Models**, and **JWT Security**.

---

## 🏛️ System Architecture

```
                                  ┌───────────────────────────┐
                                  │      Client / UI Apps     │
                                  └─────────────┬─────────────┘
                                                │
                                                ▼
                                  ┌───────────────────────────┐
                                  │   Spring Cloud Gateway    │ (Port: 8080)
                                  └─────────────┬─────────────┘
                                                │
                     ┌──────────────────────────┴──────────────────────────┐
                     ▼                                                     ▼
           Eureka Service Registry                               Spring Cloud Config Server
                 (Port: 8761)                                           (Port: 8888)
                     │
    ┌────────────────┼────────────────┬────────────────┬─────────────────┬─────────────────┐
    ▼                ▼                ▼                ▼                 ▼                 ▼
auth-service    user-service     crop-service     price-service     order-service     payment-service
 (Port: 8081)    (Port: 8082)     (Port: 8083)     (Port: 8084)      (Port: 8085)      (Port: 8086)
   auth_db         user_db          crop_db          price_db          order_db          payment_db
    │                │                │                │                 │                 │
    └────────────────┼────────────────┼────────────────┴─────────────────┴─────────────────┘
                     │                ▼
                     │       RabbitMQ Event Bus (5672)
                     │                ▲
    ┌────────────────┼────────────────┼────────────────┬─────────────────┬─────────────────┐
    ▼                ▼                ▼                ▼                 ▼                 ▼
wallet-service  bidding-service  delivery-service notification-serv  review-service    report-service
 (Port: 8087)    (Port: 8088)     (Port: 8089)     (Port: 8090)      (Port: 8091)      (Port: 8092)
  wallet_db       bidding_db      delivery_db    notification_db      review_db         report_db
    │                │                │                │                 │                 │
    └────────────────┼────────────────┴────────────────┴─────────────────┼─────────────────┘
                     ▼                                                   ▼
               audit-service                                       chatbot-service
                (Port: 8093)                                        (Port: 8094)
                  audit_db                                            chatbot_db
```

---

## 📦 Microservices Portfolio (17 Services)

| Service | Port | Database | Primary Responsibility |
|---|---|---|---|
| **config-server** | 8888 | - | Externalized Native Configuration Server |
| **eureka-server** | 8761 | - | Netflix Eureka Service Discovery |
| **api-gateway** | 8080 | - | Centralized Gateway, JWT filter, Correlation ID |
| **auth-service** | 8081 | `auth_db` | BCrypt auth, JWT token issuer, Facebook OAuth2, default admin seeder |
| **user-service** | 8082 | `user_db` | Farmer, Dealer, Delivery Partner KYC & masked bank info |
| **crop-service** | 8083 | `crop_db` | Crop catalog, geo-distance search, inventory reservation |
| **price-service** | 8084 | `price_db` | Mandi APMC prices, Unit conversions (KG/Quintal/Ton), Grade calculation |
| **order-service** | 8085 | `order_db` | Direct checkout, multi-dealer negotiation, purchase Saga initiator |
| **payment-service** | 8086 | `payment_db` | Simulated payment processing (UPI/Card/Wallet), idempotency keys |
| **wallet-service** | 8087 | `wallet_db` | Balances, double-entry ledger, delivery escrow holding & release |
| **bidding-service** | 8088 | `bidding_db` | Real-time auctions, server-side time validation, highest bid lock |
| **delivery-service** | 8089 | `delivery_db` | Delivery requests (₹10/km rate), atomic partner assignment |
| **notification-service** | 8090 | `notification_db` | RabbitMQ event listener, persisted user alerts |
| **review-service** | 8091 | `review_db` | Dealer-to-Farmer ratings (1-5 stars), reputation metrics |
| **report-service** | 8092 | `report_db` | CQRS projections for Admin, Farmer, Dealer, and Partner analytics |
| **audit-service** | 8093 | `audit_db` | Append-only audit logs for regulatory compliance |
| **chatbot-service** | 8094 | `chatbot_db` | Agricultural advisory & marketplace assistant backend |

---

## 🔑 Default Administrator Credentials

- **Email**: `mohanraj.k1110@gmail.com`
- **Password**: `Admin@123`
- **Role**: `ROLE_ADMIN`
- *Stored using BCrypt hashing; seeded on startup.*

---

## 🚀 Running the Platform

### 1. Build Entire Project via Maven
```bash
mvn clean package -DskipTests
```

### 2. Run All Automated Unit & Controller Tests
```bash
mvn test
```

### 3. Start Infrastructure & All Services via Docker Compose
```bash
docker compose up --build -d
```

---

## 📖 Detailed Documentation
- [Architecture & Ports](docs/architecture.md)
- [Business Workflows](docs/workflow.md)
- [Distributed Purchase Saga](docs/saga.md)
- [RabbitMQ Domain Events](docs/rabbitmq-events.md)
- [Database-per-Service Schemas](docs/database-design.md)
- [API Request & Response Catalog](docs/api-documentation.md)
