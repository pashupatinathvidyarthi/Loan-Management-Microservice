# Loan Management Microservice

A banking-platform style capstone project modeled directly on L&T Finance's actual
lending business (Personal, Two-Wheeler, Home, Rural Business, Farm, Gold, SME loans),
built to demonstrate the core skills listed in their **Intern – Digitization (Java
Backend)** JD: Spring Boot, REST APIs, PostgreSQL/JPA, MongoDB, Kafka, and Docker.

## Architecture

```
Client
  │  REST (JSON)
  ▼
┌─────────────────────────────────────────────────────────┐
│  Controller layer  (validation, DTO mapping, pagination) │
├─────────────────────────────────────────────────────────┤
│  Service layer      (business logic, EMI calculation)    │
├───────────────────────────┬───────────────────────────── ┤
│  PostgreSQL (JPA/Hibernate)│  MongoDB (Spring Data Mongo) │
│  Customer                  │  KycDocument                 │
│  LoanApplication            │  (flexible per-document-type │
│  Repayment                  │   metadata, no migrations)   │
└───────────────┬─────────────┴───────────────────────────┘
                │ after commit
                ▼
        Kafka: loan-application-events (3 partitions, keyed by loanApplicationId)
                │
                ▼
        CreditCheckConsumer (group: credit-check-service)
                │
                ▼
        Kafka: credit-check-results  →  (future) notification service
```

### Why relational + document store together
`Customer → LoanApplication → Repayment` is a strict, well-defined relational shape —
a natural fit for PostgreSQL with foreign keys, indexes, and pagination. KYC documents
are the opposite: a PAN card, a vehicle RC, and a gold-purity certificate all carry
different fields. Rather than a wide table full of nullable columns or a migration per
new document type, `KycDocument` stores a flexible `metadata` map in MongoDB, keyed
back to the relational core only by `customerId` / `loanApplicationId`.

### Why Kafka, and why keyed
Submitting a loan application triggers a credit check that could be slow (bureau
lookups in the real world). Publishing a `LoanApplicationSubmitted` event and returning
immediately keeps the API responsive; the `CreditCheckConsumer` does the decisioning
asynchronously. The producer **keys each message by `loanApplicationId`**, which Kafka
uses to always route that loan's events to the same partition — so even with 3
partitions and multiple consumer instances sharing the `credit-check-service` group,
a single loan's events are never processed out of order. The consumer only acts on
applications still in `PENDING` status, which makes redelivery (e.g. after a rebalance
before offset commit) a safe no-op instead of a duplicate decision.

### Why the event publish happens after commit
The Kafka publish is registered via `TransactionSynchronizationManager` to fire only
**after** the database transaction commits — so a rollback (e.g. a later validation
failure) can never result in an event being published for a loan application that
doesn't actually exist in Postgres.

## Tech stack

| Concern | Choice |
|---|---|
| Language / runtime | Java 17 |
| Framework | Spring Boot 3.2 (Web, Validation, Data JPA, Data MongoDB, Kafka, Actuator) |
| Relational DB | PostgreSQL |
| Document DB | MongoDB |
| Messaging | Apache Kafka |
| Containerization | Docker, Docker Compose |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven |

## Running it locally

**Everything in Docker (recommended):**
```bash
docker compose up --build
```
This brings up Postgres, MongoDB, Zookeeper, Kafka, and the app itself. The API is
available at `http://localhost:8080`, Swagger UI at
`http://localhost:8080/swagger-ui.html`, and health at
`http://localhost:8080/actuator/health`.

**App locally, infra in Docker** (faster edit/run loop while developing):
```bash
docker compose up postgres mongodb zookeeper kafka
mvn spring-boot:run
```

## Example requests

Create a customer:
```bash
curl -X POST http://localhost:8080/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
        "fullName": "Aditi Sharma",
        "email": "aditi.sharma@example.com",
        "phone": "9876543210",
        "panNumber": "ABCDE1234F",
        "dateOfBirth": "1998-04-12"
      }'
```

Submit a Two-Wheeler loan application (publishes a Kafka event, triggers async credit check):
```bash
curl -X POST http://localhost:8080/api/v1/loan-applications \
  -H "Content-Type: application/json" \
  -d '{
        "customerId": 1,
        "loanType": "TWO_WHEELER",
        "principalAmount": 85000,
        "interestRatePercent": 11.5,
        "tenureMonths": 24
      }'
```

Check the EMI schedule generated for it:
```bash
curl http://localhost:8080/api/v1/loan-applications/1/repayments
```

Poll status a second or two later to see the async credit-check result land:
```bash
curl http://localhost:8080/api/v1/loan-applications/1
```

Paginate loan applications by status:
```bash
curl "http://localhost:8080/api/v1/loan-applications?status=APPROVED&page=0&size=10"
```

## Live dashboard

`dashboard/dashboard.html` is a self-contained, no-build web UI (vanilla HTML/CSS/JS,
no framework) for demoing this project without curl or Swagger. It talks to the API
straight from the browser, so it needs to be served (or even just opened) from a
different origin than `localhost:8080` — `CorsConfig` exists specifically to allow that
for local development.

The standout feature: submitting a loan application from the dashboard lights up a
4-stage pipeline strip (**Submitted → Kafka event published → Consumer processing →
Decision recorded**) that polls the API every ~1.2s, so the async credit-check flow is
visible happening in real time instead of only in the terminal logs — useful for
demoing the architecture in an interview.

Easiest way to run it: open `dashboard/dashboard.html` in VS Code with the **Live
Server** extension (right-click → *Open with Live Server*), or any other local static
file server. Opening it directly as a `file://` URL can also work in most browsers,
but a local server avoids any origin-related edge cases.

## Project layout

```
src/main/java/com/ltf/loanmanagement/
├── entity/        Customer, LoanApplication, Repayment, LoanType, LoanStatus (JPA)
├── document/       KycDocument (MongoDB)
├── repository/     Spring Data JPA + MongoDB repositories
├── dto/            Request/response DTOs with Bean Validation
├── service/        Business logic (EMI calc, customer/loan/repayment/kyc services)
├── controller/      REST controllers
├── kafka/          Event payloads, topic config, producer, consumer
├── config/          Kafka consumer factory + retry/error handling
└── exception/       Custom exceptions + @RestControllerAdvice
```

## What's deliberately left as "stretch" / out of scope

To keep this project honest about what's built vs. what's a talking point for the
interview, these are NOT implemented here, on purpose:
- **Spring Security / auth** — every endpoint is open; a real deployment would add
  JWT-based auth and role-based access (Loan Officer vs Customer vs Admin).
- **ELK stack** — logs currently go to stdout only; wiring Logstash/Kibana via another
  Docker Compose service is a natural next step.
- **Kubernetes manifests** — Docker Compose covers local orchestration; a
  `deployment.yaml`/`service.yaml` pair (even against Minikube) would demonstrate the
  packaging step Kubernetes adds on top of Docker.
- **GCP Cloud Function** — e.g. generating a PDF sanction letter on approval and
  pushing it to Cloud Storage once a loan is `APPROVED`.
- **Real credit bureau integration** — `CreditCheckConsumer` uses a simple principal-
  amount heuristic in place of a real scoring service.

## Resume bullet starting points

- Architected a Spring Boot microservice modeling L&T Finance's lending lifecycle
  across a relational core (PostgreSQL/JPA — Customer → LoanApplication → Repayment)
  and a document store (MongoDB — flexible KYC metadata), exposing 15+ RESTful
  endpoints with pagination and centralized `@ControllerAdvice` error handling.
- Designed an async, Kafka-based credit-check pipeline: producer keys events by
  loanApplicationId for per-loan ordering across a 3-partition topic, consumer applies
  an idempotency guard against redelivery, and a `DefaultErrorHandler` with fixed
  backoff retries transient failures before skipping a record.
- Implemented a reducing-balance EMI amortization engine generating full repayment
  schedules (principal/interest split per installment) at loan-submission time.
- Containerized the full stack (Spring Boot app, PostgreSQL, MongoDB, Kafka,
  Zookeeper) with a multi-stage Dockerfile and Docker Compose for one-command local
  orchestration.
