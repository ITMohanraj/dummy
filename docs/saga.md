# Purchase Saga & Compensation Workflow

## Direct Purchase Saga Flow
1. **Order Initialized**: Dealer triggers `/api/v1/orders/purchase`. Order created in `PENDING` state.
2. **Crop Quantity Reserved**: Synchronous WebClient call to `crop-service` `/api/v1/crops/inventory/reserve`.
3. **Simulated Payment Executed**: `payment-service` validates transaction with idempotency key.
4. **Order Confirmed**: Order state moves to `CONFIRMED`. Dealer invoice and farmer payment receipt are generated.
5. **Event Dispatched**: `order.confirmed` emitted to RabbitMQ event bus.

## Saga Compensation on Failure
- If payment fails, compensation triggers `crop-service` `/api/v1/crops/inventory/release` to restore crop quantity.
- Order state transitions to `CANCELLED`.
