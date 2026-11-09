# FlowPay – Payment Gateway with Merchant Dashboard

<p align="center">
  <strong>A Full-Stack Payment Gateway Application Inspired by Razorpay</strong>
</p>

<p align="center">
  A learning-focused payment gateway featuring a merchant dashboard, payment processing simulation, transaction management, and event-driven backend infrastructure.
</p>

<p align="center">
  <img src="docs/screenshots/dashboard.png" alt="FlowPay Merchant Dashboard" width="100%" />
</p>

<p align="center">
  <strong>Java 21</strong> • <strong>Spring Boot 4.1.1</strong> • <strong>MySQL</strong> • <strong>Apache Kafka</strong> • <strong>Vite</strong>
</p>

---

## Overview

**FlowPay** is a full-stack payment gateway application built to explore how modern payment platforms work. Inspired by platforms like Razorpay, it combines a merchant-facing dashboard with a Java Spring Boot backend.

The application provides a centralized workspace where merchants can manage their business, create orders, monitor transactions, manage refunds and settlements, configure webhook integrations, and access their developer credentials.

The backend demonstrates practical software engineering concepts, including REST API development, merchant API-key authentication, idempotency, database transactions, payment reconciliation, ledger management, event-driven architecture, asynchronous messaging, and cryptographic webhook signatures.

FlowPay uses a **Mock Bank Service** to simulate different payment outcomes. It is an educational project and does not connect to real banking networks or process actual monetary transactions.

## Features

### Merchant Dashboard

- Merchant workspace with a centralized dashboard.
- Business profile and settings management.
- Merchant API key access.
- Webhook URL configuration.
- Merchant registration and account switching.
- Test mode interface.
- Dedicated navigation for orders, transactions, refunds, settlements, and webhooks.

### Backend and Payment Infrastructure

- Merchant registration and API-key authentication.
- Order creation and management.
- Payment creation and processing.
- Mock bank integration for payment simulation.
- Payment reconciliation for pending transactions.
- Idempotency and duplicate request protection.
- 2% platform fee calculation.
- Merchant ledger management.
- Dedicated refund and settlement modules.
- Transaction history and filtering.
- Apache Kafka integration.
- Database-backed transactional outbox infrastructure.
- HMAC-SHA256 webhook signatures.
- Webhook delivery tracking and retry mechanism.
- Spring Boot Actuator endpoints.

---

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot 4.1.1 | Backend application framework |
| Spring Web MVC | REST API development |
| Spring Data JPA | Database persistence and ORM |
| MySQL | Relational database |
| Apache Kafka | Asynchronous event messaging |
| Jakarta Validation | Request validation |
| Spring Boot Actuator | Application health and information |
| Java HttpClient | HTTP webhook delivery |
| Java Cryptography API | HMAC-SHA256 signatures |
| Maven | Backend build and dependency management |
| Vite | Frontend development and build tooling |
| H2 | Testing database dependency |

---

## Application Architecture

FlowPay follows a modular architecture that separates the merchant-facing frontend from backend business logic.

### High-Level Architecture

```text
                 MERCHANT / CLIENT
                        |
                        v
              FLOWPAY MERCHANT DASHBOARD
                        |
                        v
                 FRONTEND APPLICATION
                        |
                   REST API
                        |
                        v
              SPRING BOOT BACKEND
                        |
          +-------------+-------------+
          |             |             |
       Merchant       Orders       Payments
                                     |
                                     v
                              Mock Bank Service
                                     |
                                     v
                               Payment Status
                                     |
                    +----------------+----------------+
                    |                                 |
                  Ledger                       Event Infrastructure
                                                      |
                                                      v
                                                 Event Table
                                                      |
                                                      v
                                               Outbox Scheduler
                                                      |
                                                      v
                                                 Apache Kafka
                                                      |
                                                      v
                                                Event Consumer
                                                      |
                                                      v
                                                Webhook Record
                                                      |
                                                      v
                                             Merchant Webhook URL
                        |
                        v
                    MySQL
```

*The Kafka-to-webhook integration is partially implemented. See Known Limitations for the remaining integration work.*

### Frontend – Merchant Dashboard

The FlowPay frontend provides the interface through which merchants interact with the application.

The dashboard includes the following sections:

| Section | Functionality |
|---|---|
| Overview | Merchant workspace and dashboard |
| Orders | Order management |
| Transactions | Payment transaction history |
| Refunds | Refund management |
| Settlements | Settlement management |
| Webhooks | Webhook monitoring and management |
| Settings | Business information and developer configuration |

The settings section provides business profile management, API key access, webhook URL configuration, and merchant registration.

### Backend – Spring Boot Application

The backend is organized into separate modules using a layered architecture:

- Controllers handle incoming HTTP requests.
- Services contain business logic.
- Repositories manage database operations.
- Entities represent persistent application data.
- DTOs handle request and response data.

This modular structure keeps business responsibilities separated and makes the application easier to maintain and extend.

---

## Core Backend Modules

### 1. Merchant Management

FlowPay provides merchant registration and merchant profile management.

Capabilities:

- Register a new merchant.
- Generate merchant API credentials.
- Authenticate merchant-specific requests.
- Retrieve authenticated merchant details.
- Update merchant information.
- Configure webhook URLs and webhook secrets.
- Support multiple merchant accounts.

Merchant-specific API requests use the `X-Api-Key` request header.

### 2. Order Management

Merchants can create and manage payment orders.

Capabilities:

- Create payment orders.
- Retrieve individual order details.
- List merchant orders.
- Associate payments with merchant-owned orders.
- Prevent payments against orders that are already paid or have an active payment.

### 3. Payment Processing

The payment lifecycle is managed by `PaymentService` and `MockBankService`.

A merchant creates a payment against an existing order. FlowPay validates the request, checks idempotency, and prevents duplicate active payments before saving the payment.

The payment initially receives a `CREATED` status. When the payment process endpoint is called, the mock bank service simulates the payment outcome.

#### Payment Simulation Scenarios

| Scenario | Initial Status | Resolution |
|---|---|---|
| `SUCCESS` | `SUCCESS` | Immediately successful |
| `FAILURE` | `FAILED` | Immediately failed |
| `DELAYED_SUCCESS` | `PENDING` | Resolves to `SUCCESS` after 8 seconds |
| `TIMEOUT` | `PENDING` | Resolves to `SUCCESS` after 20 seconds |

The simulation uses Java switch expressions.

```java
public Result initiate(String scenario) {
    return switch (scenario) {
        case "SUCCESS" ->
            new Result("SUCCESS", null);

        case "FAILURE" ->
            new Result("FAILED", null);

        case "DELAYED_SUCCESS" ->
            new Result("PENDING", Instant.now().plusSeconds(8));

        case "TIMEOUT" ->
            new Result("PENDING", Instant.now().plusSeconds(20));

        default ->
            throw new IllegalArgumentException(
                "Unknown Simulation scenario"
            );
    };
}
```

#### Payment Reconciliation

For pending payments, FlowPay provides a reconciliation operation.

```java
public String check(Instant resolveAt) {
    return resolveAt != null &&
           !Instant.now().isBefore(resolveAt)
           ? "SUCCESS"
           : "PENDING";
}
```

The reconciliation endpoint checks whether the configured resolution time has passed and updates the payment status when the result is available.

**Important:** Both `DELAYED_SUCCESS` and `TIMEOUT` eventually resolve to `SUCCESS` in the current mock implementation. `TIMEOUT` represents a longer pending period, not a permanently failed payment.

#### Payment Statuses

| Status | Description |
|---|---|
| `CREATED` | Payment created but not processed |
| `PENDING` | Payment awaiting final resolution |
| `SUCCESS` | Payment completed successfully |
| `FAILED` | Payment processing failed |

### 4. Idempotency

Idempotency prevents duplicate resource creation when clients retry requests because of network failures or uncertain responses.

FlowPay implements idempotency checks for payment creation and accepts idempotency keys for refund and settlement creation.

Payment creation follows these steps:

1. Validate the idempotency key.
2. Search for an existing payment using merchant ID and idempotency key.
3. Compare the original order, payment method, and scenario.
4. Return the existing payment if the request matches.
5. Reject the request with a conflict if the same key is reused with different request details.

Merchant locking is also used during payment creation.

Example request headers:

```http
X-Api-Key: YOUR_API_KEY
Idempotency-Key: unique-payment-request-001
```

### 5. Platform Fee and Ledger Management

FlowPay calculates a 2% platform fee on successful payments.

The fee calculation uses integer arithmetic and rounds upward to the next whole unit.

```java
public static long fee(long amount) {
    return (Math.multiplyExact(amount, 2) + 99) / 100;
}
```

The merchant's net amount is calculated as:

```text
Platform Fee = 2% of Payment Amount

Merchant Net Amount = Payment Amount - Platform Fee
```

When a payment succeeds:

- Payment status becomes `SUCCESS`.
- Associated order is marked `PAID`.
- Platform fee is calculated.
- Merchant net amount is recorded in the ledger.

Example:

| Description | Amount |
|---|---:|
| Payment Amount | ₹1,000 |
| Platform Fee (2%) | ₹20 |
| Merchant Net Amount | ₹980 |

### 6. Refund Management

FlowPay contains a dedicated refund module with REST endpoints for refund creation and retrieval.

Refund creation accepts:

- `X-Api-Key`
- `Idempotency-Key`

The refund implementation is maintained separately from payment processing.

### 7. Settlement Management

FlowPay contains a dedicated settlement module for settlement creation and retrieval.

Settlement creation accepts:

- `X-Api-Key`
- `Idempotency-Key`

These headers support merchant authentication and duplicate-request protection.

### 8. Transaction Management

FlowPay provides transaction APIs to retrieve payment records and ledger entries.

Transaction listing supports optional filters:

- `status`
- `from`
- `to`

The transaction listing endpoint returns payment records, while a separate ledger endpoint provides ledger entries.

### 9. Kafka and Event-Driven Architecture

FlowPay contains event infrastructure built around a database-backed event table, scheduled outbox publishing, and Apache Kafka.

#### Event Service

`EventService` creates event records containing:

- Event ID
- Merchant ID
- Event type
- Reference ID
- Amount
- Currency (`INR`)
- Creation timestamp

The event payload is serialized into JSON and stored in the database.

#### Transactional Outbox Pattern

FlowPay uses an event table to store events before they are published to Kafka.

**OutBoxJobs:**

- Runs at a fixed delay of 3 seconds.
- Fetches up to 50 unpublished events.
- Passes events to the outbox publisher.
- Stops the current batch if publishing fails.

**OutBoxPublisher:**

- Locks the event record.
- Checks whether it has already been published.
- Sends the Kafka message.
- Updates the `publishedAt` timestamp after successful publication.

This approach separates event persistence from asynchronous message publishing.

#### Kafka Consumer

`EventListener` listens to the configured Kafka topic and delegates processing to `EventConsumerService`.

The consumer:

- Retrieves and locks the event record.
- Checks whether it has already been consumed.
- Loads the associated merchant.
- Creates a webhook record.
- Marks the webhook `SKIPPED` if no webhook URL is configured.
- Updates the event's `consumedAt` timestamp.

#### Kafka Configuration

| Configuration | Value |
|---|---|
| Topic | `flowpay.events` |
| Partitions | 3 |
| Replicas | 1 |
| Consumer Group | `flowpay-webhooks` |
| Offset Reset | `earliest` |
| Auto Commit | Disabled |
| Listener Acknowledgment Mode | `record` |
| Error Handler Backoff | 2 seconds |
| Error Handler Attempts | Unlimited |

Kafka components are conditionally enabled using:

```properties
app.kafka-enabled=true
```

### 10. Webhook System

FlowPay includes webhook infrastructure for notifying merchants about event-related updates.

It supports webhook record creation, HTTP delivery, signature generation, delivery status tracking, and manual retries.

#### HMAC-SHA256 Signature

FlowPay signs outgoing webhook payloads using the merchant's webhook secret and HMAC-SHA256.

```java
Mac mac = Mac.getInstance("HmacSHA256");

mac.init(new SecretKeySpec(
    secret.getBytes(StandardCharsets.UTF_8),
    "HmacSHA256"
));

String signature = HexFormat.of()
    .formatHex(
        mac.doFinal(payload.getBytes(StandardCharsets.UTF_8))
    );
```

The generated signature is included in the outgoing request.

#### Webhook Request Headers

| Header | Purpose |
|---|---|
| `Content-Type` | Identifies the JSON request body |
| `X-FlowPay-Event` | Identifies the event |
| `X-FlowPay-Signature` | Contains the HMAC-SHA256 signature |

#### Webhook Delivery

FlowPay uses Java's built-in `HttpClient` for HTTP delivery.

| Configuration | Value |
|---|---|
| HTTP Client | Java HttpClient |
| Connection Timeout | 2 seconds |
| Request Timeout | 3 seconds |
| Redirects | Disabled |
| Successful Response | HTTP 2xx |

Before sending the webhook, the configured URL is validated through the merchant service.

#### Retry Mechanism

Failed webhook requests are retried using exponential backoff.

The retry delay is calculated using:

```java
1L << attempts
```

The webhook service:

1. Increments the attempt count.
2. Sends the event payload to the merchant endpoint.
3. Marks the webhook `DELIVERED` if the response is HTTP 2xx.
4. Stores the last error if delivery fails.
5. Keeps the webhook `PENDING` while attempts remain.
6. Marks the webhook `FAILED` after five unsuccessful attempts.

Merchants can manually retry an undelivered webhook through the retry endpoint. Manual retry resets the attempt count and next attempt time.

#### Webhook Statuses

| Status | Description |
|---|---|
| `PENDING` | Waiting for delivery or retry |
| `DELIVERED` | Successfully delivered |
| `FAILED` | Maximum automatic attempts reached |
| `SKIPPED` | No webhook URL configured |

---

## REST API Documentation

FlowPay exposes **19 REST API endpoints** grouped into seven resource modules.

### Merchant APIs

Base path: `/api/merchants`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/merchants` | Register a merchant |
| GET | `/api/merchants/me` | Retrieve authenticated merchant details |
| PUT | `/api/merchants/me` | Update merchant details |

### Order APIs

Base path: `/api/orders`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Create an order |
| GET | `/api/orders` | List merchant orders |
| GET | `/api/orders/{id}` | Retrieve order details |

### Payment APIs

Base path: `/api/payments`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/payments` | Create a payment |
| GET | `/api/payments/{id}` | Retrieve payment details |
| POST | `/api/payments/{id}/process` | Process a payment |
| POST | `/api/payments/{id}/check` | Reconcile a pending payment |

### Refund APIs

Base path: `/api/refunds`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/refunds` | Create a refund |
| GET | `/api/refunds` | List refunds |

### Settlement APIs

Base path: `/api/settlements`

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/settlements` | Create a settlement |
| GET | `/api/settlements` | List settlements |

### Transaction APIs

Base path: `/api/transactions`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/transactions` | List transactions |
| GET | `/api/transactions/ledger` | Retrieve ledger entries |
| GET | `/api/transactions/{id}` | Retrieve transaction details |

Optional transaction filters:

```text
status
from
to
```

### Webhook APIs

Base path: `/api/webhooks`

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/webhooks` | List merchant webhooks |
| POST | `/api/webhooks/{id}/retry` | Retry a webhook |

### API Authentication

Merchant-specific endpoints use the `X-Api-Key` header.

```http
X-Api-Key: YOUR_API_KEY
```

Payment, refund, and settlement creation endpoints also accept an `Idempotency-Key` header.

```http
X-Api-Key: YOUR_API_KEY
Idempotency-Key: unique-request-key
Content-Type: application/json
```

---

## Screenshots

Screenshots of the FlowPay merchant dashboard and backend workflows are maintained in `docs/screenshots/`.

### Merchant Dashboard

![FlowPay Dashboard](docs/screenshots/dashboard.png)

### Business Settings and Developer Connection

![FlowPay Settings](docs/screenshots/settings.png)

### Orders

![FlowPay Orders](docs/screenshots/orders.png)

### Transactions

![FlowPay Transactions](docs/screenshots/transactions.png)

### Refunds

![FlowPay Refunds](docs/screenshots/refunds.png)

### Settlements

![FlowPay Settlements](docs/screenshots/settlements.png)

### Webhook Management

![FlowPay Webhooks](docs/screenshots/webhooks.png)

### API Testing and Payment Processing

Add Postman screenshots demonstrating payment creation, successful and failed payment responses, and payment reconciliation.

---

## Getting Started

### Prerequisites

Install the following tools:

- Java 21
- Maven
- Node.js and npm
- MySQL
- Apache Kafka
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/FlowPay.git

cd FlowPay
```

### 2. Configure MySQL

Start your MySQL server and configure the database connection.

Example `application.properties` configuration:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/flowpay?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC

spring.datasource.username=${DB_USER:root}
spring.datasource.password=${DB_PASSWORD}
```

The `createDatabaseIfNotExist=true` option allows MySQL to create the database if it does not already exist, provided the configured database user has the required permissions.

### 3. Configure Environment Variables

For macOS or Linux:

```bash
export DB_USER=root
export DB_PASSWORD=your_mysql_password
export KAFKA_SERVERS=127.0.0.1:9092
export DEMO_ENABLED=true
```

Never commit actual database credentials, API keys, or webhook secrets to GitHub.

### 4. Start Apache Kafka

Make sure your Kafka broker is running at the configured address:

```text
127.0.0.1:9092
```

When Kafka integration is enabled, FlowPay configures the `flowpay.events` topic.

### 5. Start the Backend

Open a terminal in the project root:

```bash
./mvnw spring-boot:run
```

Alternatively, use Maven:

```bash
mvn spring-boot:run
```

The backend starts on its configured Spring Boot server port. The example webhook configuration uses port `8081`; check your actual `server.port` setting.

### 6. Start the Frontend

Open another terminal:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the Vite development server:

```bash
npm run dev
```

The frontend development server runs at:

```text
http://localhost:5173
```

Make sure the frontend API configuration points to the correct backend address.

---

## Project Structure

```text
FlowPay/
│
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│
├── docs/
│   └── screenshots/
│       ├── dashboard.png
│       ├── settings.png
│       ├── orders.png
│       ├── transactions.png
│       ├── refunds.png
│       ├── settlements.png
│       └── webhooks.png
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/cfs/FlowPay/
│   │   │       │
│   │   │       ├── bank/
│   │   │       │   ├── MockBankService.java
│   │   │       │   └── BankJobs.java
│   │   │       │
│   │   │       ├── common/
│   │   │       ├── merchant/
│   │   │       ├── order/
│   │   │       ├── payment/
│   │   │       ├── refund/
│   │   │       ├── settelment/
│   │   │       ├── transaction/
│   │   │       ├── webhook/
│   │   │       │
│   │   │       └── kafka/
│   │   │           ├── Event.java
│   │   │           ├── EventService.java
│   │   │           ├── EventConsumerService.java
│   │   │           ├── EventListener.java
│   │   │           ├── EventRepository.java
│   │   │           ├── KafkaConfig.java
│   │   │           ├── OutBoxJobs.java
│   │   │           └── OutBoxPublisher.java
│   │   │
│   │   └── resources/
│   │       └── application.properties
│   │
│   └── test/
│
├── pom.xml
└── README.md
```

*This is a representative structure. Exact frontend and backend files may vary as development continues.*

---

## Engineering Concepts Demonstrated

FlowPay brings together practical full-stack and backend engineering concepts:

- Merchant-facing dashboard development.
- Frontend-to-backend REST API integration.
- REST API design and HTTP methods.
- Layered architecture using controllers, services, and repositories.
- Merchant API-key authentication.
- Database persistence using Spring Data JPA.
- Transaction management using `@Transactional`.
- Idempotency and duplicate request handling.
- Database locking during critical operations.
- Payment lifecycle and state transitions.
- Mock bank integration and payment reconciliation.
- Integer-based platform fee calculation.
- Ledger recording.
- Event-driven architecture with Apache Kafka.
- Database-backed transactional outbox publishing.
- Kafka consumer processing and error handling.
- HMAC-SHA256 cryptographic signatures.
- HTTP webhook delivery and retry handling.
- Environment-based application configuration.

## Known Limitations and Future Improvements

The following improvements are planned to make FlowPay's end-to-end workflow more complete:

1. **Complete payment event publishing:** The `event.create()` calls inside `PaymentService.finish()` are currently commented out. Connect event creation to successful and failed payment outcomes.
2. **Fix Kafka message identification:** `OutBoxPublisher` currently sends `e.merchantId` as the Kafka message value, but `EventListener` passes the received value to `consume(String id)`, which expects an event ID. Publish `e.id` as the message value.
3. **Complete automatic delayed-payment handling:** `BankJobs.completeDelayed()` currently has an empty method body. Implement its scheduled reconciliation logic if automatic completion is required. Pending payments can currently be resolved through the reconciliation API.
4. **Verify end-to-end webhook delivery:** Test the full path from payment outcome to event persistence, Kafka publication, webhook creation, and HTTP delivery.
5. **Add automated integration tests:** Cover payment processing, idempotency, reconciliation, Kafka consumption, and webhook retry scenarios.
6. **Improve production security:** Review API-key storage, authorization, webhook URL validation, and secret management.
7. **Improve deployment:** Add Docker and Docker Compose for easier local setup.
8. **Improve observability:** Add structured logging, metrics, tracing, and monitoring.
9. **Real payment provider integration:** Add a separate integration with a real payment provider if the project is extended beyond simulation.

---

## ScreenShots 
<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 48 52 PM" src="https://github.com/user-attachments/assets/c9730d1b-3937-48b8-87c9-770ce473f584" />
<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 18 25 PM" src="https://github.com/user-attachments/assets/403eb75c-4ada-4065-a196-831459fd675f" />
<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 19 00 PM" src="https://github.com/user-attachments/assets/f86d53f2-dd51-48b0-9b5a-b2d4c04cb91c" />

<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 19 08 PM" src="https://github.com/user-attachments/assets/0e4565f0-13b5-4440-9b7b-bd0cd556ef51" />

<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 20 13 PM" src="https://github.com/user-attachments/assets/d998de72-d06a-43d0-ae0b-c0a173074fe4" />
<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 20 24 PM" src="https://github.com/user-attachments/assets/aff670fe-67d5-49ca-b104-e1d13ca58f40" />

<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 20 36 PM" src="https://github.com/user-attachments/assets/a5296e0f-efc3-4ba4-996b-84fa793210d8" />
<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 20 54 PM" src="https://github.com/user-attachments/assets/42a953be-e4a1-4c22-a71c-0af914fbec5c" />
<img width="1470" height="956" alt="Screenshot 2026-11-09 at 8 21 31 PM" src="https://github.com/user-attachments/assets/7364e811-faf3-42de-9f1a-e5ca8644b15a" />

---

## Author

**Sushil Verma**

B.Tech Computer Science & Engineering '27

Aspiring Java Developer | Backend Engineering

- GitHub: [GitHub Profile](https://github.com/SushilVerma18)
- LinkedIn: [LinkedIn Profile](linkedin.com/in/sushil-verma18)

---

<p align="center">
  <strong>FlowPay</strong>
  <br/>
  Built to explore payment gateway workflows and modern full-stack engineering.
  <br/><br/>
  <i>Made with Java, Spring Boot, and a passion for backend engineering.</i>
</p>
