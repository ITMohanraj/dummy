# RabbitMQ Domain Events Reference

| Event Name | Routing Key | Exchange | Producers | Consumers |
|---|---|---|---|---|
| `USER_REGISTERED` | `user.registered` | `auth.exchange` | auth-service | notification-service, audit-service |
| `CROP_POSTED` | `crop.posted` | `crop.exchange` | crop-service | price-service (alerts), notification-service |
| `ORDER_CONFIRMED` | `order.confirmed` | `order.exchange` | order-service | notification-service, report-service |
| `PAYMENT_SUCCESS` | `payment.success` | `payment.exchange` | payment-service | wallet-service, notification-service |
| `DELIVERY_DELIVERED`| `delivery.delivered`| `delivery.exchange` | delivery-service | wallet-service (escrow release) |
| `BID_ACCEPTED` | `bid.accepted` | `bidding.exchange` | bidding-service | order-service, notification-service |
| `REVIEW_CREATED` | `review.created` | `review.exchange` | review-service | audit-service |
