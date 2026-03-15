# Microservices Learning Project

A Spring Boot microservices learning project demonstrating a basic microservice architecture with three independent services communicating via REST APIs.

## 1. Project Overview

This project demonstrates a basic microservice architecture built using Spring Boot. The system consists of three independent microservices that communicate synchronously using REST APIs. This architecture serves as a foundation for learning microservices concepts and will later evolve using Spring Cloud technologies.

The project showcases:
- Service decomposition and separation of concerns
- Inter-service communication via HTTP REST APIs
- Orchestration patterns in microservices
- Service isolation with independent databases

## 2. Architecture

The system follows a simple orchestration pattern where the **order-service** acts as the orchestrator, coordinating interactions between services to fulfill order creation requests.

### Request Flow

```
Client → Order Service → User Service
Client → Order Service → Payment Service
```

### Architecture Diagram

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ HTTP REST
       │
┌──────▼──────────────────────────────────────┐
│         Order Service (Orchestrator)        │
│  ┌──────────────────────────────────────┐   │
│  │  • Validates user via user-service   │   │
│  │  • Processes payment via payment-    │   │
│  │    service                           │   │
│  │  • Manages order lifecycle          │   │
│  └──────────────────────────────────────┘   │
└──────┬──────────────────┬───────────────────┘
       │                  │
       │ HTTP REST        │ HTTP REST
       │                  │
┌──────▼──────┐    ┌──────▼──────────┐
│ User Service│    │ Payment Service │
│             │    │                 │
│ • User CRUD │    │ • Payment       │
│ • User      │    │   Processing    │
│   Management│    │ • Payment       │
│             │    │   Status        │
└─────────────┘    └─────────────────┘
```

### Service Responsibilities

- **order-service**: Orchestrates the workflow by coordinating user validation and payment processing
- **user-service**: Manages user data and provides user information
- **payment-service**: Handles payment processing and payment status management

## 3. Services

### user-service

The user service is responsible for managing user data and providing user information to other services.

**Endpoints:**
- `POST /users` - Create a new user
- `GET /users` - Get all users
- `GET /users/{id}` - Get user by ID
- `PUT /users/{id}` - Update user by ID
- `DELETE /users/{id}` - Delete user by ID

**Entity & DTO:**
- Uses `User` entity with fields: `id`, `name`, `role`, `location`
- Uses `UserDto` for data transfer between services
- Implements proper separation between entity and DTO layers

**Default Port:** `8080`

### payment-service

The payment service processes payments and manages payment status. It receives payment requests from the order service and returns payment confirmation.

**Endpoints:**
- `POST /payments` - Process a new payment
- `GET /payments/{id}` - Get payment by ID
- `GET /payments/order/{orderId}` - Get all payments for a specific order

**Payment Entity:**
- Fields: `id`, `orderId`, `userId`, `amount`, `currency`, `status`, `createdAt`
- Uses `PaymentStatus` enum with values: `PENDING`, `SUCCESS`, `FAILED`
- Payment status is managed through the enum to ensure type safety

**Default Port:** `8081`

### order-service

The order service orchestrates the order creation workflow by:
1. Validating the user exists through user-service
2. Processing payment through payment-service
3. Storing order information locally

**Endpoints:**
- `POST /orders` - Create a new order (orchestrates user validation and payment)
- `GET /orders/{id}` - Get order by ID
- `GET /orders` - Get all orders

**Order Creation Flow:**
1. Receives order request with `userId`, `amount`, and `currency`
2. Validates user exists by calling `user-service`
3. Creates initial order with `PENDING` status
4. Calls `payment-service` to process payment
5. Updates order with payment ID and final status
6. Returns order response with complete information

**Default Port:** `8082` (default Spring Boot port)

## 4. Communication Model

Services currently communicate via **synchronous HTTP REST calls** using Spring Boot's `RestTemplate`. The order-service uses REST clients (`UserClient` and `PaymentClient`) to make HTTP requests to other services.

### Request Flow for Creating an Order

1. **Client** sends `POST /orders` request to **order-service**
2. **order-service** validates user by calling `GET /users/{id}` on **user-service**
3. **order-service** creates a temporary order with `PENDING` status
4. **order-service** processes payment by calling `POST /payments` on **payment-service**
5. **order-service** updates the order with payment information and final status
6. **order-service** returns the complete order response to the client

### Communication Pattern

- **Synchronous**: All inter-service communication is synchronous (request-response)
- **Direct HTTP**: Services communicate directly via HTTP without service discovery
- **RESTful**: All services expose RESTful APIs following standard HTTP methods
- **Client Pattern**: Order-service uses dedicated client classes (`UserClient`, `PaymentClient`) to encapsulate service-to-service communication

## 5. Technology Stack

- **Java 17** - Programming language
- **Spring Boot 3.3.2 / 4.0.3** - Application framework
- **Spring Data JPA** - Data persistence layer
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **Gradle** - Build automation tool
- **REST APIs** - Communication protocol
- **Spring Web** - Web framework for REST endpoints
- **Spring Actuator** - Production-ready features for monitoring

## 6. Running the Project

Each service is an independent Spring Boot application and must be run separately.

### Prerequisites

- Java 17 or higher
- Gradle (or use the included Gradle wrapper)

### Running Services

Navigate to each service directory and run:

```bash
# Terminal 1 - User Service
cd user-service
./gradlew bootRun

# Terminal 2 - Payment Service
cd payment-service
./gradlew bootRun

# Terminal 3 - Order Service
cd order-service
./gradlew bootRun
```

### Service Ports

- **user-service**: `8080`
- **payment-service**: `8081`
- **order-service**: `8082` (default Spring Boot port)

**Note:** Ensure all services are running before making requests to the order-service, as it depends on both user-service and payment-service.

## 7. API Testing

### Creating a User

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "role": "Customer",
    "location": "New York"
  }'
```

### Creating a Payment

```bash
curl -X POST http://localhost:8081/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "amount": 99.99,
    "currency": "USD"
  }'
```

### Creating an Order

```bash
curl -X POST http://localhost:8082/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "amount": 99.99,
    "currency": "USD"
  }'
```

**Note:** When creating an order, ensure the `userId` exists in the user-service. The order-service will automatically validate the user and process the payment.

### Additional Examples

**Get all users:**
```bash
curl http://localhost:8080/users
```

**Get user by ID:**
```bash
curl http://localhost:8080/users/1
```

**Get payment by ID:**
```bash
curl http://localhost:8081/payments/1
```

**Get payments by order ID:**
```bash
curl http://localhost:8081/payments/order/1
```

**Get order by ID:**
```bash
curl http://localhost:8082/orders/1
```

**Get all orders:**
```bash
curl http://localhost:8082/orders
```

## 8. What This Project Demonstrates

This project serves as a learning resource for understanding key microservices concepts:

### Key Concepts

- **Microservice Decomposition**: Breaking down a monolithic application into smaller, focused services
- **Service-to-Service Communication**: Implementing HTTP-based communication between services
- **Orchestration Pattern**: Using a central service (order-service) to coordinate workflow across multiple services
- **REST API Contracts**: Defining and implementing RESTful APIs for service interaction
- **Service Isolation**: Each service maintains its own database and can be developed/deployed independently
- **DTO Pattern**: Using Data Transfer Objects to decouple internal entities from API contracts
- **Client Pattern**: Encapsulating inter-service communication in dedicated client classes

### Learning Outcomes

- Understanding how to structure microservices
- Implementing synchronous service communication
- Managing service dependencies and orchestration
- Working with Spring Boot REST clients
- Applying separation of concerns in distributed systems

## 9. Future Improvements

This project is designed to evolve with additional Spring Cloud technologies and patterns:

### Planned Enhancements

- **Service Discovery (Spring Cloud Eureka)**: Implement service registry to eliminate hardcoded URLs
- **Feign Clients**: Replace RestTemplate with declarative Feign clients for cleaner service communication
- **API Gateway (Spring Cloud Gateway)**: Add a single entry point for all client requests
- **Resilience Patterns (Resilience4j)**: Implement circuit breakers, retries, and rate limiting
- **Messaging Systems (Kafka/RabbitMQ)**: Introduce asynchronous communication for better scalability
- **Saga Pattern**: Implement distributed transaction management for complex workflows
- **Configuration Management**: Use Spring Cloud Config Server for centralized configuration
- **Distributed Tracing**: Add observability with Spring Cloud Sleuth and Zipkin
- **Load Balancing**: Implement client-side load balancing for service instances
- **Security**: Add OAuth2/JWT for service-to-service authentication

### Evolution Path

1. **Phase 1 (Current)**: Direct HTTP communication with RestTemplate
2. **Phase 2**: Service discovery and Feign clients
3. **Phase 3**: API Gateway and resilience patterns
4. **Phase 4**: Asynchronous messaging and event-driven architecture
5. **Phase 5**: Advanced patterns (Saga, CQRS, etc.)

## 10. Project Structure

Each service follows a standard Spring Boot project structure:

```
UOP/
├── user-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/spring/userservice/
│   │   │   │   ├── controller/
│   │   │   │   │   └── UserController.java
│   │   │   │   ├── dto/
│   │   │   │   │   └── UserDto.java
│   │   │   │   ├── entities/
│   │   │   │   │   └── User.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── UserRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   └── UserService.java
│   │   │   │   └── UserServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── build.gradle
│   └── settings.gradle
│
├── order-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/spring/orderservice/
│   │   │   │   ├── client/
│   │   │   │   │   ├── PaymentClient.java
│   │   │   │   │   └── UserClient.java
│   │   │   │   ├── config/
│   │   │   │   │   └── RestTemplateConfig.java
│   │   │   │   ├── controller/
│   │   │   │   │   └── OrderController.java
│   │   │   │   ├── dto/
│   │   │   │   │   ├── OrderRequestDto.java
│   │   │   │   │   ├── OrderResponseDto.java
│   │   │   │   │   ├── PaymentRequestDto.java
│   │   │   │   │   ├── PaymentResponseDto.java
│   │   │   │   │   └── UserDto.java
│   │   │   │   ├── entity/
│   │   │   │   │   └── Order.java
│   │   │   │   ├── repository/
│   │   │   │   │   └── OrderRepository.java
│   │   │   │   ├── service/
│   │   │   │   │   └── OrderService.java
│   │   │   │   └── OrderServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.properties
│   │   └── test/
│   ├── build.gradle
│   └── settings.gradle
│
└── payment-service/
    ├── src/
    │   ├── main/
    │   │   ├── java/com/spring/paymentservice/
    │   │   │   ├── controller/
    │   │   │   │   └── PaymentController.java
    │   │   │   ├── dto/
    │   │   │   │   ├── PaymentRequestDto.java
    │   │   │   │   └── PaymentResponseDto.java
    │   │   │   ├── entity/
    │   │   │   │   └── Payment.java
    │   │   │   ├── enums/
    │   │   │   │   └── PaymentStatus.java
    │   │   │   ├── repository/
    │   │   │   │   └── PaymentRepository.java
    │   │   │   ├── service/
    │   │   │   │   └── PaymentService.java
    │   │   │   └── PaymentServiceApplication.java
    │   │   └── resources/
    │   │       └── application.properties
    │   └── test/
    ├── build.gradle
    └── settings.gradle
```

### Common Patterns

- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Data access using Spring Data JPA
- **DTO Layer**: Data transfer objects for API contracts
- **Entity Layer**: JPA entities for database mapping
- **Client Layer** (order-service only): REST clients for inter-service communication

---

## Contributing

This is a learning project. Feel free to experiment, modify, and extend the services to explore different microservices patterns and technologies.

## License

This project is for educational purposes.
