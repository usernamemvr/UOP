# Microservices Learning Project

A Spring Boot microservices learning project demonstrating a microservice architecture with service discovery and API Gateway, featuring five independent services communicating via REST APIs with Spring Cloud Eureka and Spring Cloud Gateway.

## 1. Project Overview

This project demonstrates a microservice architecture built using Spring Boot and Spring Cloud. The system consists of five independent services that communicate synchronously using REST APIs with **Spring Cloud Eureka** for service discovery and **Spring Cloud Gateway** as a single entry point. This architecture serves as a foundation for learning microservices concepts and continues to evolve with additional Spring Cloud technologies such as **Spring Cloud OpenFeign** and **Spring Cloud Gateway**.

The project showcases:
- Service decomposition and separation of concerns
- **API Gateway Pattern** - Single entry point for all client requests via Spring Cloud Gateway
- **Service discovery and registration with Spring Cloud Eureka**
- **Load-balanced inter-service communication** via Eureka-aware HTTP clients
- Inter-service communication via HTTP REST APIs using service names (through **OpenFeign** clients in `order-service`)
- Orchestration patterns in microservices
- Service isolation with independent databases
- **Reactive Gateway** - Non-blocking I/O with Spring WebFlux

## 2. Architecture

The system follows a simple orchestration pattern where the **order-service** acts as the orchestrator, coordinating interactions between services to fulfill order creation requests.

### Request Flow

```
Client → Gateway Service → Order Service → User Service
Client → Gateway Service → Order Service → Payment Service
Client → Gateway Service → User Service
Client → Gateway Service → Payment Service
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
│      Gateway Service (API Gateway)          │
│      Port: 8086                             │
│  ┌──────────────────────────────────────┐   │
│  │  • Single entry point                │   │
│  │  • Route: /users/** → user-service    │   │
│  │  • Route: /orders/** → order-service │   │
│  │  • Route: /payments/** → payment-svc   │   │
│  │  • Load-balanced routing (lb://)      │   │
│  │  • Eureka service discovery          │   │
│  └──────────────────────────────────────┘   │
└──────┬──────────┬──────────┬────────────────┘
       │          │          │
       │          │          │ HTTP REST
       │          │          │ (via Eureka)
       │          │          │
       │          │    ┌─────▼──────────────────┐
       │          │    │ Order Service           │
       │          │    │ (Orchestrator)          │
       │          │    │ Port: 8082              │
       │          │    │ ┌────────────────────┐ │
       │          │    │ │ • Validates user    │ │
       │          │    │ │ • Processes payment│ │
       │          │    │ │ • Uses Feign       │ │
       │          │    │ └────────────────────┘ │
       │          │    └─────┬──────────┬───────┘
       │          │          │          │
       │          │          │ Feign    │ Feign
       │          │          │ Client   │ Client
       │          │          │          │
       │          │    ┌─────▼──┐  ┌───▼────────┐
       │          │    │ User    │  │ Payment    │
       │          │    │ Service │  │ Service    │
       │          │    │ 8080    │  │ 8085       │
       │          │    └─────────┘  └────────────┘
       │          │
       │    ┌─────▼──────────┐
       │    │ User Service    │
       │    │ Port: 8080      │
       │    │ • User CRUD     │
       │    └─────────────────┘
       │
       └─────▼──────────────┐
            │ Payment Service│
            │ Port: 8085     │
            │ • Payment Proc │
            └─────────────────┘
                  │
                  │   Register & Discover
                  │
         ┌────────▼─────────┐
         │  Eureka Server   │
         │  (Discovery)      │
         │  Port: 8761      │
         └──────────────────┘
```

### Service Responsibilities

- **gateway-service**: Single entry point for all client requests, routes to appropriate microservices with load balancing
- **order-service**: Orchestrates the workflow by coordinating user validation and payment processing
- **user-service**: Manages user data and provides user information
- **payment-service**: Handles payment processing and payment status management
- **discovery-server**: Service registry and discovery server (Eureka)

## 3. Services

### gateway-service

The gateway service acts as the single entry point (API Gateway) for all client requests. It routes requests to the appropriate microservices using Spring Cloud Gateway with Eureka service discovery.

**Technology:**
- **Spring Cloud Gateway** - Reactive API Gateway
- **Spring WebFlux** - Non-blocking reactive framework
- **Eureka Client** - Service discovery integration

**Features:**
- Routes all external requests to backend services
- Load-balanced routing using `lb://` protocol
- Dynamic service discovery via Eureka
- Reactive and non-blocking I/O

**Route Configuration:**
- `/users/**` → `lb://user-service` (load-balanced)
- `/orders/**` → `lb://order-service` (load-balanced)
- `/payments/**` → `lb://payment-service` (load-balanced)

**Default Port:** `8086`

**Note:** All client requests should go through the gateway service. Direct access to individual services is still possible but not recommended for production.

### user-service

The user service is responsible for managing user data and providing user information to other services.

**Endpoints:**
- `POST /users/create` - Create a new user
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

**Default Port:** `8085`

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

## 4. Service Discovery

The project uses **Spring Cloud Eureka** for service discovery. All microservices register themselves with the Eureka server, allowing services to discover and communicate with each other using service names instead of hardcoded URLs.

### Eureka Server (Discovery Server)

The Eureka server acts as a service registry where all microservices register themselves and discover other services.

- **Port**: `8761`
- **Dashboard**: `http://localhost:8761` - View all registered services
- **Service Registry**: All services register with Eureka and can discover each other by service name

### Service Registration

All services are configured with:
- `@EnableDiscoveryClient` annotation
- Eureka client configuration pointing to `http://localhost:8761/eureka/`
- Service names defined in `application.yml`:
  - `gateway-service` (port 8086)
  - `user-service` (port 8080)
  - `payment-service` (port 8085)
  - `order-service` (port 8082)

### Load Balancing

Client-side load balancing is handled by the Spring Cloud stack and Eureka when multiple instances of a service are registered, enabling:
- Service name resolution through Eureka
- Client-side load balancing across multiple service instances
- Dynamic service discovery without hardcoded URLs

## 5. Communication Model

All client requests first go through the **Gateway Service**, which routes them to the appropriate microservices. Services communicate via **synchronous HTTP REST calls** using **Spring Cloud OpenFeign** clients with **Eureka service discovery**. The order-service uses Feign-based REST clients (`UserClient` and `PaymentClient`) that resolve service names through Eureka.

### Request Flow for Creating an Order (via Gateway)

1. **Client** sends `POST /orders` request to **gateway-service** (port 8086)
2. **gateway-service** routes the request to **order-service** using load-balanced routing (`lb://order-service`)
3. **order-service** validates user by calling `GET /users/{id}` on **user-service** via an **OpenFeign client** (`UserClient`) with service name resolved through Eureka
4. **order-service** creates a temporary order with `PENDING` status
5. **order-service** processes payment by calling `POST /payments` on **payment-service** via an **OpenFeign client** (`PaymentClient`) with service name resolved through Eureka
6. **order-service** updates the order with payment information and final status
7. **order-service** returns the complete order response through **gateway-service** to the client

### Communication Pattern

- **API Gateway Pattern**: All external requests go through the gateway-service, providing a single entry point
- **Synchronous**: All inter-service communication is synchronous (request-response)
- **Service Discovery**: Services communicate using service names resolved through Eureka
- **Load Balanced**: Gateway uses `lb://` protocol for load-balanced routing; Eureka enables client-side load balancing when scaled
- **Reactive Gateway**: Gateway service uses Spring WebFlux for non-blocking, reactive I/O
- **RESTful**: All services expose RESTful APIs following standard HTTP methods
- **Client Pattern**: Order-service uses dedicated **OpenFeign client** interfaces (`UserClient`, `PaymentClient`) to encapsulate service-to-service communication
- **Service Names**: Inter-service calls use service names (e.g., `http://user-service/users`, `http://payment-service/payments`) instead of hardcoded URLs

## 6. Technology Stack

- **Java 17** - Programming language
- **Spring Boot (3.2.x / 3.3.x / 3.5.x)** - Application framework (per service)
  - discovery-server: **Spring Boot 3.5.11**
  - gateway-service: **Spring Boot 3.2.5**
  - user-service: **Spring Boot 3.3.2**
  - order-service: **Spring Boot 3.2.5**
  - payment-service: **Spring Boot 3.2.5**
- **Spring Cloud Eureka** - Service discovery and registration
- **Spring Cloud Netflix Eureka Client** - Service registration for each microservice
- **Spring Cloud Gateway** - Reactive API Gateway for routing and load balancing (gateway-service)
- **Spring Cloud OpenFeign** - Declarative HTTP clients for inter-service communication (order-service)
- **Spring WebFlux** - Reactive web framework for non-blocking I/O (gateway-service)
- **Spring Cloud Dependencies 2023.0.3 / 2025.0.1** - Spring Cloud BOMs used across services
- **Spring Data JPA** - Data persistence layer
- **H2 Database** - In-memory database for development
- **Lombok** - Reduces boilerplate code
- **Gradle** - Build automation tool
- **REST APIs** - Communication protocol
- **Spring Web** - Web framework for REST endpoints
- **Spring Actuator** - Production-ready features for monitoring

## 7. Running the Project

Each service is an independent Spring Boot application and must be run separately. **The Eureka discovery server must be started first** before starting the microservices.

### Prerequisites

- Java 17 or higher
- Gradle (or use the included Gradle wrapper)

### Running Services

**Important:** Start the Eureka server first, then start the microservices in any order.

```bash
# Terminal 1 - Eureka Discovery Server (START THIS FIRST)
cd discovery-server
./gradlew bootRun

# Terminal 2 - User Service
cd user-service
./gradlew bootRun

# Terminal 3 - Payment Service
cd payment-service
./gradlew bootRun

# Terminal 4 - Order Service
cd order-service
./gradlew bootRun

# Terminal 5 - Gateway Service (START AFTER OTHER SERVICES)
cd gateway-service
./gradlew bootRun
```

### Service Ports

- **discovery-server (Eureka)**: `8761` - Service registry and dashboard
- **gateway-service**: `8086` - API Gateway (single entry point)
- **user-service**: `8080`
- **payment-service**: `8085`
- **order-service**: `8082`

### Verifying Service Registration

1. Start the Eureka server and wait for it to be ready
2. Start all microservices
3. Visit `http://localhost:8761` to view the Eureka dashboard
4. You should see all four services registered:
   - `GATEWAY-SERVICE`
   - `USER-SERVICE`
   - `PAYMENT-SERVICE`
   - `ORDER-SERVICE`

**Note:** 
- Ensure the Eureka server is running before starting microservices
- Start the gateway-service after other services are registered with Eureka
- All services must be running before making requests to the order-service, as it depends on both user-service and payment-service
- Services will automatically register with Eureka on startup
- **All client requests should go through the gateway-service (port 8086)**

## 8. API Testing

**Important:** All API requests should go through the **Gateway Service** on port **8086**. The gateway routes requests to the appropriate backend services.

### Gateway Service Endpoints (Port: 8086)

All endpoints below are accessed through the gateway. The gateway automatically routes to the correct service.

### Order Service Endpoints (via Gateway: Port 8086)

#### Create Order
```bash
curl -X POST http://localhost:8086/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "amount": 100.50,
    "currency": "USD"
  }'
```

#### Get Order by ID
```bash
curl -X GET http://localhost:8086/orders/1
```

#### Get All Orders
```bash
curl -X GET http://localhost:8086/orders
```

### Payment Service Endpoints (via Gateway: Port 8086)

#### Create Payment
```bash
curl -X POST http://localhost:8086/payments \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 1,
    "userId": 1,
    "amount": 100.50,
    "currency": "USD"
  }'
```

#### Get Payment by ID
```bash
curl -X GET http://localhost:8086/payments/1
```

#### Get Payments by Order ID
```bash
curl -X GET http://localhost:8086/payments/order/1
```

### User Service Endpoints (via Gateway: Port 8086)

#### Create User
```bash
curl -X POST http://localhost:8086/users/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "role": "Customer",
    "location": "New York"
  }'
```

#### Get All Users
```bash
curl -X GET http://localhost:8086/users
```

#### Get User by ID
```bash
curl -X GET http://localhost:8086/users/1
```

#### Update User
```bash
curl -X PUT http://localhost:8086/users/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Doe",
    "role": "Premium Customer",
    "location": "Los Angeles"
  }'
```

#### Delete User
```bash
curl -X DELETE http://localhost:8086/users/1
```

### Example Complete Flow (via Gateway)

1. **Create a user first (via Gateway):**
```bash
curl -X POST http://localhost:8086/users/create \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "role": "Customer",
    "location": "New York"
  }'
```

2. **Create an order via Gateway (automatically validates user and processes payment):**
```bash
curl -X POST http://localhost:8086/orders \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "amount": 250.75,
    "currency": "USD"
  }'
```
**What happens:**
- Gateway routes request to `order-service`
- `order-service` validates user via `user-service` (Feign client)
- `order-service` processes payment via `payment-service` (Feign client)
- Response returns through gateway to client

3. **Get the created order (via Gateway):**
```bash
curl -X GET http://localhost:8086/orders/1
```

4. **Get payments for that order (via Gateway):**
```bash
curl -X GET http://localhost:8086/payments/order/1
```

**Note:** 
- All requests go through the gateway-service (port 8086)
- When creating an order, ensure the `userId` exists in the user-service
- The order-service automatically:
  - Discovers and calls user-service via Eureka to validate the user
  - Discovers and calls payment-service via Eureka to process the payment
- All inter-service communication uses service names resolved through Eureka
- Gateway provides load-balanced routing to service instances

## 9. What This Project Demonstrates

This project serves as a learning resource for understanding key microservices concepts:

### Key Concepts

- **API Gateway Pattern**: Single entry point for all client requests, providing routing, load balancing, and abstraction
- **Microservice Decomposition**: Breaking down a monolithic application into smaller, focused services
- **Service Discovery**: Using Eureka for dynamic service registration and discovery
- **Service-to-Service Communication**: Implementing HTTP-based communication between services using service names
- **Load Balancing**: Gateway and Eureka enable load-balanced routing across multiple service instances
- **Orchestration Pattern**: Using a central service (order-service) to coordinate workflow across multiple services
- **Reactive Gateway**: Non-blocking, reactive API Gateway using Spring WebFlux
- **REST API Contracts**: Defining and implementing RESTful APIs for service interaction
- **Service Isolation**: Each service maintains its own database and can be developed/deployed independently
- **DTO Pattern**: Using Data Transfer Objects to decouple internal entities from API contracts
- **Client Pattern**: Encapsulating inter-service communication in dedicated client classes (OpenFeign)

### Learning Outcomes

- Understanding how to structure microservices
- Implementing API Gateway pattern with Spring Cloud Gateway
- Implementing service discovery with Spring Cloud Eureka
- Configuring reactive, non-blocking API Gateway with Spring WebFlux
- Configuring load-balanced routing in API Gateway
- Configuring load-balanced REST clients for service communication
- Implementing synchronous service communication with service discovery
- Managing service dependencies and orchestration
- Working with Spring Boot REST clients and service name resolution
- Applying separation of concerns in distributed systems

## 10. Future Improvements

This project is designed to evolve with additional Spring Cloud technologies and patterns:

### Planned Enhancements

- **Feign Clients**: Replace RestTemplate with declarative Feign clients for cleaner service communication ✅ (implemented in `order-service`)
- **API Gateway (Spring Cloud Gateway)**: Add a single entry point for all client requests ✅ (implemented)
- **Resilience Patterns (Resilience4j)**: Implement circuit breakers, retries, and rate limiting
- **Messaging Systems (Kafka/RabbitMQ)**: Introduce asynchronous communication for better scalability
- **Saga Pattern**: Implement distributed transaction management for complex workflows
- **Configuration Management**: Use Spring Cloud Config Server for centralized configuration
- **Distributed Tracing**: Add observability with Spring Cloud Sleuth and Zipkin
- **Security**: Add OAuth2/JWT for service-to-service authentication
- **Service Mesh**: Consider implementing Istio or Linkerd for advanced service-to-service communication

### Evolution Path

1. **Phase 1 (Completed)**: Direct HTTP communication with RestTemplate
2. **Phase 2 (Completed)**: Service discovery with Eureka and load-balanced HTTP clients
3. **Phase 3 (Completed)**: Feign clients in `order-service` and API Gateway with Spring Cloud Gateway ✅
4. **Phase 4 (Current)**: Resilience patterns and distributed tracing
5. **Phase 5**: Asynchronous messaging and event-driven architecture
6. **Phase 6**: Advanced patterns (Saga, CQRS, etc.)

## 11. Project Structure

Each service follows a standard Spring Boot project structure:

```
UOP/
├── discovery-server/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── (Eureka Server Application)
│   │   │   └── resources/
│   │   │       └── application.yml
│   ├── build.gradle
│   └── settings.gradle
│
├── gateway-service/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/spring/gatewayservice/
│   │   │   │   └── GatewayServiceApplication.java
│   │   │   └── resources/
│   │   │       └── application.yaml
│   ├── build.gradle
│   └── settings.gradle
│
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
│   │   │       └── application.yml
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
│   │   │       └── application.yml
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
    │   │       └── application.yml
    │   └── test/
    ├── build.gradle
    └── settings.gradle
```

### Common Patterns

- **Gateway Layer** (gateway-service): Routes all external requests to backend services with load balancing
- **Controller Layer**: Handles HTTP requests and responses
- **Service Layer**: Contains business logic
- **Repository Layer**: Data access using Spring Data JPA
- **DTO Layer**: Data transfer objects for API contracts
- **Entity Layer**: JPA entities for database mapping
- **Client Layer** (order-service only): OpenFeign clients for inter-service communication using service names
- **Discovery Client**: All services register with and discover services through Eureka

---

## Contributing

This is a learning project. Feel free to experiment, modify, and extend the services to explore different microservices patterns and technologies.

## License

This project is for educational purposes.
