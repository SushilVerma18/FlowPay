# FlowPay – Payment Gateway with Merchant Dashboard

<p align="center">
  <strong>A Full-Stack Payment Gateway Application Inspired by Razorpay</strong>
</p>

<p align="center">
  A learning-focused payment gateway featuring a merchant dashboard, payment processing simulation, transaction management, and event-driven backend infrastructure.
</p>

<p align="center">
  <img width="1600" height="906" alt="WhatsApp Image 2026-11-09 at 9 07 30 PM" src="https://github.com/user-attachments/assets/f57a0ca5-ca80-4849-819f-e4f4f74b6fbe" />

</p>

<p align="center">
  <strong>Java 21</strong> • <strong>Spring Boot 4.1.1</strong> • <strong>MySQL</strong> • <strong>Apache Kafka</strong> • <strong>Vite</strong>
</p>

---

# FlowPay – Full-Stack Payment Gateway with Merchant Dashboard

<p align="center">
  <strong>A Full-Stack Payment Gateway Application Inspired by Razorpay</strong>
</p>

<p align="center">
  Java 21 • Spring Boot • React.js • Vite • MySQL • Apache Kafka • REST APIs • HMAC-SHA256
</p>

---

## Overview

**FlowPay** is a full-stack payment gateway application inspired by modern payment platforms such as Razorpay. It provides a merchant-oriented dashboard backed by a Java Spring Boot application for managing payment operations and financial transactions.

The application brings together a React-based merchant dashboard and a Spring Boot backend to demonstrate how payment gateway systems manage merchants, orders, payments, refunds, settlements, transaction ledgers, and webhook integrations.

FlowPay explores important backend engineering concepts, including:

- Idempotent API design
- Transactional database operations
- Merchant-specific authentication and authorization
- Payment lifecycle management
- Transaction ledger and balance management
- Event-driven architecture
- Kafka messaging and Outbox pattern
- HMAC-based webhook signing
- Automated webhook retries and failure handling

**Important:** FlowPay is an educational payment gateway simulation. It uses a mock bank service rather than a real banking or payment network.

## Application Architecture

FlowPay follows a full-stack architecture in which the merchant dashboard communicates with the backend through REST APIs.

### Frontend – Merchant Dashboard

The React-based dashboard provides a merchant-facing interface for accessing payment operations and managing merchant information.

Key dashboard areas include:

- Merchant registration and profile management
- Merchant business settings
- API-key-based access
- Webhook URL configuration
- Order management
- Payment and transaction management
- Refund management
- Settlement management
- Transaction and ledger information

### Backend – Payment Processing System

The Spring Boot backend handles business logic, authentication, payment workflows, database operations, financial records, and webhook delivery.

Its major modules include:

- Merchant Management
- Order Management
- Payment Processing
- Transaction Ledger
- Refund Management
- Settlement Management
- Kafka and Outbox Architecture
- Webhook Delivery
- API Validation and Monitoring

## Key Features

### 1. Merchant Dashboard

- React-based merchant dashboard.
- Merchant registration and profile management.
- Business settings and webhook configuration.
- Merchant API-key-based access.
- Navigation for orders, transactions, refunds, and settlements.
- Integration with backend REST APIs.

### 2. Merchant Management

- Merchant registration and profile management.
- API-key-based authentication using the `X-Api-Key` request header.
- SHA-256 hashing of stored API keys.
- Merchant-specific data access.
- Webhook URL configuration and host validation.

### 3. Order Management

- Create payment orders.
- Retrieve individual orders and merchant order lists.
- Track order status, including `CREATED` and `PAID`.
- Validate order amount and receipt input.

### 4. Payment Processing

- Create and retrieve payments.
- Support for UPI, CARD, WALLET, and NETBANKING payment methods.
- Integrated mock bank service for payment simulation.

Supported simulated bank outcomes:

- `SUCCESS`
- `FAILURE`
- `DELAYED_SUCCESS`
- `TIMEOUT`

Additional capabilities:

- Payment status tracking.
- Manual reconciliation of pending payments.
- Merchant-scoped payment idempotency.

### 5. Idempotent Payment APIs

FlowPay accepts an `Idempotency-Key` header when creating payments.

- Validates idempotency key format.
- Enforces uniqueness per merchant at the database level.
- Returns the existing payment for repeated requests with matching parameters.
- Rejects reuse of the same key with different request parameters.
- Prevents creating another active payment for an order.

### 6. Transaction Ledger and Merchant Balance

- Maintains merchant balance updates through a dedicated ledger service.
- Records payment credits after fee deduction.
- Stores transaction reference, transaction type, amount, and resulting balance.
- Uses exact arithmetic for financial balance updates.
- Supports ledger history retrieval.

### 7. Refund Management

- Create refunds against successful payments.
- Supports partial refunds.
- Uses merchant-scoped refund idempotency keys.
- Prevents refunds exceeding the remaining refundable amount.
- Calculates corresponding fee adjustments.
- Updates merchant balance and records refund ledger entries.

### 8. Settlement Management

- Create settlements using the merchant's available balance.
- Uses idempotency keys for settlement requests.
- Records settlement debits in the ledger.
- Provides settlement history.

### 9. Kafka and Outbox Architecture

The backend includes components for an event-driven architecture.

- Persistent event records.
- Scheduled Outbox polling.
- Kafka topic configuration.
- Kafka producer and consumer.
- Event-consumption tracking.
- Transactional creation of webhook records from consumed events.

**Implementation status:** The event publishing and consumption components are present in the code. Payment and refund lifecycle event creation still needs to be connected to the corresponding service flows, and the Kafka publisher's message value needs to be corrected to use the event ID before describing the complete pipeline as operational.

### 10. Webhook Delivery

The webhook module supports outgoing event delivery to merchant-configured endpoints.

Features include:

- HMAC-SHA256 payload signing.
- Event identification through the `X-FlowPay-Event` header.
- Signature delivery through the `X-FlowPay-Signature` header.
- HTTP POST delivery to configured merchant endpoints.
- HTTP response validation.
- Scheduled retry attempts with exponential backoff.
- Failed-delivery tracking.
- Manual webhook retry.
- Protection against unapproved webhook hosts through an allowlist.

Webhook delivery is an outgoing integration. The merchant's receiver-side processing and signature verification are outside this backend implementation.

### 11. API Validation and Monitoring

- Jakarta Bean Validation for request DTOs.
- Centralized exception handling using `@RestControllerAdvice`.
- Consistent JSON error responses.
- Spring Boot Actuator health and info endpoints.

---

## Technology Stack

### Frontend

| Category | Technology |
|---|---|
| Library | React.js |
| Build Tool | Vite |
| Communication | REST API integration |
| Development Server | Vite |

### Backend

| Category | Technology |
|---|---|
| Language | Java 21 |
| Backend Framework | Spring Boot 4.1.1 |
| REST APIs | Spring Web MVC |
| ORM / Persistence | Spring Data JPA, Hibernate |
| Database | MySQL |
| Messaging | Apache Kafka |
| Kafka Integration | Spring Kafka |
| Validation | Jakarta Bean Validation |
| Monitoring | Spring Boot Actuator |
| Build Tool | Maven |
| Testing Dependencies | Spring Boot Test Starters, H2 |

---

## System Architecture

```text
                 MERCHANT DASHBOARD
                       |
                       v
                React.js + Vite
                       |
                       | REST API / HTTP
                       v
                SPRING BOOT BACKEND
                       |
          +------------+------------+
          |            |            |
          v            v            v
      Merchant       Orders       Payments
     Management    Management    Processing
          |            |            |
          +------------+------------+
                       |
                       v
                TRANSACTION SERVICES
                       |
            +----------+----------+
            |          |          |
            v          v          v
          Ledger    Refunds   Settlements
            |
            v
           MySQL
            |
            v
        EVENT / OUTBOX
            |
            v
           Kafka
            |
            v
       EVENT CONSUMER
            |
            v
      WEBHOOK RECORDS
            |
            v
      WEBHOOK DELIVERY
            |
            v
      MERCHANT ENDPOINT
```

*Note: The diagram illustrates the intended component architecture. Payment/refund-to-event integration and the Kafka message-value issue described above must be resolved before the complete event-to-webhook pipeline can be considered operational.*

---

## Project Structure

The application separates the frontend dashboard from the backend service.

### Frontend

```text
frontend/
│
├── package.json
├── vite.config.js
└── src/
    └── React application source files
```

### Backend

```text
src/main/java/com/cfs/FlowPay/
│
├── bank/
│   ├── BankJobs.java
│   └── MockBankService.java
│
├── common/
│   ├── ApiException.java
│   ├── BaseEntity.java
│   ├── ErrorHandler.java
│   ├── Idempotency.java
│   └── Ids.java
│
├── kafka/
│   ├── Event.java
│   ├── EventConsumerService.java
│   ├── EventListener.java
│   ├── EventRepository.java
│   ├── EventService.java
│   ├── KafkaConfig.java
│   ├── OutBoxJobs.java
│   └── OutBoxPublisher.java
│
├── merchant/
│   ├── Merchant.java
│   ├── MerchantController.java
│   ├── MerchantDtos.java
│   ├── MerchantRepository.java
│   └── MerchantService.java
│
├── order/
│   ├── OrderController.java
│   ├── OrderDtos.java
│   ├── OrderService.java
│   ├── PaymentOrder.java
│   └── PaymentOrderRepository.java
│
├── payment/
│   ├── Payment.java
│   ├── PaymentController.java
│   ├── PaymentDtos.java
│   ├── PaymentRepository.java
│   └── PaymentService.java
│
├── refund/
│   ├── Refund.java
│   ├── RefundController.java
│   ├── RefundDtos.java
│   ├── RefundRepository.java
│   └── RefundService.java
│
├── settelment/
│   ├── Settlement.java
│   ├── SettlementController.java
│   ├── SettlementRepository.java
│   └── SettlementService.java
│
├── transaction/
│   ├── LedgerEntry.java
│   ├── LedgerEntryRepository.java
│   ├── LedgerService.java
│   ├── TransactionController.java
│   └── TransactionService.java
│
└── webhook/
    ├── Webhook.java
    ├── WebhookController.java
    ├── WebhookJobs.java
    ├── WebhookRepository.java
    └── WebhookService.java
```

---

## Getting Started

Follow these instructions to run the FlowPay frontend and backend locally.

### Prerequisites

Install the following:

- Java 21
- Node.js and npm
- Maven
- MySQL
- Apache Kafka (when Kafka processing is enabled)

### 1. Clone the Repository

```bash
git clone YOUR_REPOSITORY_URL
cd FlowPay
```

Replace `YOUR_REPOSITORY_URL` with your actual GitHub repository URL.

### 2. Configure the MySQL Database

Create the database:

```sql
CREATE DATABASE flowpay;
```

Make sure the MySQL server is running.

### 3. Configure Backend Environment Variables

Configure your local environment variables rather than committing credentials.

Example:

```bash
export DB_USER=root
export DB_PASSWORD=your_mysql_password
export KAFKA_SERVERS=127.0.0.1:9092
export WEBHOOK_HOSTS=localhost,127.0.0.1,backend
```

The application also supports the following properties:

| Property | Purpose |
|---|---|
| `app.jobs-enabled` | Enables scheduled jobs |
| `app.kafka-enabled` | Enables Kafka components |
| `app.demo-enabled` | Controls demo-related configuration |
| `app.webhook-hosts` | Comma-separated webhook host allowlist |

Ensure these environment variables correspond to the placeholders configured in your `application.properties`.

### 4. Start MySQL and Kafka

Make sure MySQL is running.

Start the Kafka broker at the address configured in your application properties if Kafka processing is enabled.

Kafka components are conditional on:

```properties
app.kafka-enabled=true
```

### 5. Run the Backend

Navigate to the backend project directory:

```bash
./mvnw spring-boot:run
```

Alternatively, if Maven is installed:

```bash
mvn spring-boot:run
```

The Spring Boot application will start using the configured database and application properties.

### 6. Run the Frontend

Open another terminal and navigate to the frontend directory:

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

Open the frontend in your browser:

```text
http://localhost:5173/
```

The frontend and backend must both be running for the complete application workflow.

---

## API Reference

All merchant-protected endpoints require the following header:

```http
X-Api-Key: YOUR_API_KEY
```

Payment, refund, and settlement creation endpoints also require an idempotency key where applicable:

```http
Idempotency-Key: unique-request-key
```

### Merchant APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/merchants` | Register a merchant |
| GET | `/api/merchants/me` | Retrieve merchant details |
| PUT | `/api/merchants/me` | Update merchant profile and webhook URL |

### Order APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/orders` | Create an order |
| GET | `/api/orders` | List merchant orders |
| GET | `/api/orders/{id}` | Retrieve an order |

### Payment APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/payments` | Create a payment |
| GET | `/api/payments/{id}` | Retrieve payment details |
| POST | `/api/payments/{id}/process` | Process a payment |
| POST | `/api/payments/{id}/check` | Reconcile a pending payment |

### Refund APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/refunds` | Create a refund |
| GET | `/api/refunds` | List merchant refunds |

### Settlement APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/settlements` | Create a settlement |
| GET | `/api/settlements` | List merchant settlements |

### Transaction and Ledger APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/transactions` | List payments with optional filters |
| GET | `/api/transactions/{id}` | Retrieve payment details |
| GET | `/api/transactions/ledger` | Retrieve ledger entries |

The transaction listing supports optional `status`, `from`, and `to` query parameters.

Example:

```http
GET /api/transactions?status=SUCCESS&from=2026-01-01&to=2026-01-31
```

### Webhook APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/webhooks` | List webhook delivery records |
| POST | `/api/webhooks/{id}/retry` | Retry a webhook delivery |

### Monitoring APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/actuator/health` | Application health |
| GET | `/actuator/info` | Application information |

---

## Payment Flow

The payment lifecycle follows these steps:

1. A merchant registers through FlowPay.
2. The merchant receives an API key for authenticated operations.
3. The merchant creates a payment order.
4. The merchant creates a payment using an idempotency key.
5. The payment is processed through the mock bank service.
6. The mock bank returns a simulated payment result.
7. Successful payments update the order status and merchant ledger.
8. Pending payments can be checked through the reconciliation endpoint.
9. Refunds can be initiated against successful payments.
10. Settlements debit the merchant's available balance and create corresponding ledger entries.

---

## Webhook Signing

FlowPay signs outgoing webhook payloads using HMAC-SHA256.

Each webhook request includes:

```http
Content-Type: application/json
X-FlowPay-Event: event_id
X-FlowPay-Signature: generated_signature
```

The signature is generated using the raw event payload and the merchant's webhook secret.

A receiving merchant can independently calculate the HMAC-SHA256 signature and compare it with the supplied signature to verify payload authenticity.

---

## Testing

The Maven configuration includes Spring Boot testing dependencies and H2 for test-scope database support.

Run backend tests using:

```bash
mvn test
```

Or:

```bash
./mvnw test
```

---

## Security Considerations

FlowPay includes several security-oriented implementation practices:

- API keys are stored as SHA-256 hashes rather than plaintext.
- Merchant-specific API access is enforced in service operations.
- Idempotency keys reduce duplicate payment, refund, and settlement requests.
- Webhook payloads are signed using HMAC-SHA256.
- Webhook destinations are checked against a configured host allowlist.
- Webhook requests use connection and request timeouts.
- Webhook HTTP redirects are not followed.
- Request DTOs use Jakarta Bean Validation.
- Centralized exception handling provides consistent error responses.

**Security limitation:** This is a learning implementation, not a production-ready payment platform. Further security review, DNS-resolution and private-network protections, concurrency testing, operational monitoring, and integration with a regulated payment provider would be required before handling real transactions.

---

## Future Improvements

- Complete payment and refund lifecycle event creation.
- Correct and test Kafka event publishing.
- Complete and test the end-to-end Outbox-to-webhook pipeline.
- Add automated completion for delayed mock-bank payments.
- Add comprehensive integration and concurrency tests.
- Improve webhook delivery observability and failure handling.
- Add pagination for transaction and ledger history.
- Implement stronger webhook destination protections, including private-network and DNS-resolution checks.
- Improve production-level logging and monitoring.
- Integrate a real payment provider in a controlled test environment.

---





## ScreenShots 
<img width="1600" height="850" alt="WhatsApp Image 2026-11-09 at 9 04 28 PM" src="https://github.com/user-attachments/assets/0ffe425c-b839-4e66-87b0-f562c2c922c5" />
<img width="1600" height="909" alt="WhatsApp Image 2026-11-09 at 9 05 25 PM" src="https://github.com/user-attachments/assets/90d42aac-139f-4adb-8d8a-203c89cf0cca" />
<img width="1600" height="908" alt="WhatsApp Image 2026-11-09 at 9 06 41 PM" src="https://github.com/user-attachments/assets/ffcc99b5-9cdc-4a3a-a3dd-0a967ae5b987" />
<img width="1600" height="906" alt="WhatsApp Image 2026-11-09 at 9 07 30 PM" src="https://github.com/user-attachments/assets/27056a99-0eb9-4be7-b9e9-e2dbc77cb6d6" />
<img width="1600" height="899" alt="WhatsApp Image 2026-11-09 at 9 08 53 PM" src="https://github.com/user-attachments/assets/ec184b65-5278-40e9-8ffd-dcfe7f9ee490" />
<img width="1600" height="902" alt="WhatsApp Image 2026-11-09 at 9 09 30 PM" src="https://github.com/user-attachments/assets/80aacacd-fe4e-4e02-8985-151dd85ed818" />
<img width="1600" height="906" alt="WhatsApp Image 2026-11-09 at 9 10 13 PM" src="https://github.com/user-attachments/assets/62b244a8-5c0a-4cd8-87ea-214e0dc8d5d5" />
<img width="1600" height="906" alt="WhatsApp Image 2026-11-09 at 9 10 52 PM" src="https://github.com/user-attachments/assets/76ca53c1-4761-4f88-acdd-24b71bd6055e" />
<img width="1600" height="909" alt="WhatsApp Image 2026-11-09 at 9 11 52 PM" src="https://github.com/user-attachments/assets/8762f60d-6c8e-4fc0-bd3f-7ce7d0d875b4" />
<img width="1600" height="900" alt="WhatsApp Image 2026-11-09 at 9 12 25 PM" src="https://github.com/user-attachments/assets/e289313f-0f9b-46cd-87ea-cca6ffde2c57" />

---

## Disclaimer

FlowPay is an educational full-stack project inspired by payment gateway architecture.

It does not connect to real banks, process actual card or UPI transactions, or move real money.

The mock bank service is used exclusively to simulate payment outcomes and demonstrate payment lifecycle management.

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
