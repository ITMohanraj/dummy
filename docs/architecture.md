# CropDeal Architecture & Port Map

CropDeal implements true Database-per-Service autonomy with 14 isolated MySQL schemas and zero cross-database joins or queries.

## Port Map & Health Endpoints
- **API Gateway**: `http://localhost:8080/actuator/health`
- **Eureka Dashboard**: `http://localhost:8761`
- **RabbitMQ Management**: `http://localhost:15672` (guest/guest)
- **Auth Service**: `http://localhost:8081/swagger-ui.html`
- **User Service**: `http://localhost:8082/swagger-ui.html`
- **Crop Service**: `http://localhost:8083/swagger-ui.html`
- **Price Service**: `http://localhost:8084/swagger-ui.html`
- **Order Service**: `http://localhost:8085/swagger-ui.html`
- **Payment Service**: `http://localhost:8086/swagger-ui.html`
- **Wallet Service**: `http://localhost:8087/swagger-ui.html`
- **Bidding Service**: `http://localhost:8088/swagger-ui.html`
- **Delivery Service**: `http://localhost:8089/swagger-ui.html`
- **Notification Service**: `http://localhost:8090/swagger-ui.html`
- **Review Service**: `http://localhost:8091/swagger-ui.html`
- **Report Service**: `http://localhost:8092/swagger-ui.html`
- **Audit Service**: `http://localhost:8093/swagger-ui.html`
- **Chatbot Service**: `http://localhost:8094/swagger-ui.html`
