# Feedback API

A Spring Boot REST API service that handles feedback submissions, persists data to PostgreSQL, and publishes events to Kafka. 
This service serves as the core backend for the Provider Feedback Portal.

## Table of Contents

- [Overview](#overview)
- [Architecture & Role](#architecture--role)
- [Codebase Structure](#codebase-structure)
- [Prerequisites](#prerequisites)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)

---

## Overview

The Feedback API is a microservice that:

- **Accepts** HTTP REST requests for feedback submission and retrieval
- **Validates** input data according to business rules (service-layer validation)
- **Persists** feedback to PostgreSQL database
- **Publishes** events to Kafka topic `feedback-submitted` for downstream processing
- **Provides** Swagger/OpenAPI documentation for interactive testing
- **Handles** errors gracefully with structured error responses

This service follows a layered architecture with clear separation of concerns: controllers handle HTTP, services contain business logic, repositories manage data access, and messaging components handle event publishing.

---

## Architecture & Role

### How It Fits in the System

```
┌─────────────┐
│  React UI   │
│  (Frontend) │
└──────┬──────┘
       │ HTTP REST
       ▼
┌─────────────────────┐
│   Feedback API       │ ← You are here
│  (This Service)      │
└──────┬───────────────┘
       │
       ├──► PostgreSQL (persistence)
       │
       └──► Kafka Topic: feedback-submitted
                  │
                  ▼
         ┌─────────────────┐
         │ Analytics        │
         │ Consumer         │
         └─────────────────┘
```

### Data Flow

1. **Frontend** sends HTTP POST request to `/api/v1/feedback`
2. **Controller** receives request and delegates to service
3. **Service** validates input data (memberId, providerName, rating, comment)
4. **Service** maps request DTO to entity and saves to PostgreSQL
5. **Service** publishes event to Kafka topic `feedback-submitted`
6. **Controller** returns response with created feedback (including generated UUID and timestamp)

---

## Codebase Structure

```
tsg-9.27-devinjosh-feedback-api/
├── src/
│   ├── main/
│   │   ├── java/net/yorksolutions/tsgfeedbackapi/
│   │   │   ├── TsgfeedbackapiApplication.java          # Main application class
│   │   │   ├── config/
│   │   │   │   └── CorsConfig.java                     # CORS configuration
│   │   │   ├── controllers/
│   │   │   │   ├── FeedbackController.java            # REST endpoints
│   │   │   │   └── GlobalExceptionHandler.java         # Exception handling
│   │   │   ├── services/
│   │   │   │   ├── FeedbackService.java                # Business logic & validation
│   │   │   │   ├── ValidationException.java            # Custom validation exception
│   │   │   │   └── FeedbackNotFoundException.java       # Not found exception
│   │   │   ├── repositories/
│   │   │   │   ├── FeedbackRepository.java              # Spring Data JPA repository
│   │   │   │   └── entities/
│   │   │   │       └── FeedbackEntity.java              # JPA entity
│   │   │   ├── dtos/
│   │   │   │   ├── FeedbackRequest.java                # Request DTO
│   │   │   │   ├── FeedbackResponse.java                # Response DTO
│   │   │   │   ├── ErrorResponse.java                  # Error response DTO
│   │   │   │   └── contracts/
│   │   │   │       └── FeedbackSubmittedEvent.java     # Kafka event contract
│   │   │   └── messaging/
│   │   │       ├── KafkaProducerConfig.java            # Kafka producer configuration
│   │   │       └── FeedbackEventPublisher.java          # Event publisher service
│   │   └── resources/
│   │       └── application.yml                         # Application configuration
│   └── test/
│       └── java/net/yorksolutions/tsgfeedbackapi/
│           ├── controllers/
│           │   └── FeedbackControllerTest.java          # Controller tests (MockMvc)
│           └── services/
│               └── FeedbackServiceTest.java             # Service unit tests
├── Dockerfile                                           # Container image
├── pom.xml                                              # Maven dependencies
├── mvnw / mvnw.cmd                                     # Maven wrapper
└── README.md                                            # This file
```

### Key Components

#### 1. `FeedbackController.java`
- **Purpose**: REST controller handling HTTP requests
- **Endpoints**:
  - `POST /api/v1/feedback` - Submit new feedback
  - `GET /api/v1/feedback/{id}` - Get feedback by UUID
  - `GET /api/v1/feedback?memberId={id}` - Get feedback by member ID
  - `GET /api/v1/health` - Health check
- **Features**: Swagger annotations, proper HTTP status codes, thin controller pattern

#### 2. `FeedbackService.java`
- **Purpose**: Business logic layer with validation
- **Key Features**:
  - Service-layer validation (memberId, providerName, rating, comment)
  - Transactional operations
  - DTO to Entity mapping
  - Event publishing coordination
- **Validation Rules**:
  - `memberId`: Required, non-empty, max 36 characters
  - `providerName`: Required, non-empty, max 80 characters
  - `rating`: Required, integer 1-5
  - `comment`: Optional, max 200 characters

#### 3. `FeedbackRepository.java`
- **Purpose**: Data access layer (Spring Data JPA)
- **Methods**:
  - `findById(UUID)` - Find by primary key
  - `findByMemberId(String)` - Find all feedback for a member
  - `saveAndFlush(FeedbackEntity)` - Save and flush immediately

#### 4. `FeedbackEntity.java`
- **Purpose**: JPA entity representing database table
- **Table**: `feedback`
- **Fields**: id (UUID), memberId, providerName, rating, comment, submittedAt
- **Features**: Auto-generated UUID, automatic timestamp via `@CreationTimestamp`

#### 5. `FeedbackEventPublisher.java`
- **Purpose**: Publishes events to Kafka
- **Topic**: `feedback-submitted`
- **Key**: Feedback UUID (as String)
- **Value**: `FeedbackSubmittedEvent` (JSON)

#### 6. `GlobalExceptionHandler.java`
- **Purpose**: Centralized exception handling
- **Handles**:
  - `ValidationException` → 400 Bad Request
  - `FeedbackNotFoundException` → 404 Not Found
  - `MethodArgumentNotValidException` → 400 Bad Request
- **Response Format**: `ErrorResponse` with field errors array

#### 7. `CorsConfig.java`
- **Purpose**: CORS configuration for frontend access
- **Allowed Origins**: localhost:5173, localhost:5174, localhost:3000
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS

---

## Prerequisites

### Required Software

- **Java 21** (JDK or JRE)
  - Verify: `java -version`
- **Maven 3.9+** (or use included `mvnw` wrapper)
  - Verify: `./mvnw --version` or `mvn --version`

### Infrastructure

- **PostgreSQL** (required for running the application)
  - Can use Docker Compose from parent project
  - Or standalone PostgreSQL installation
  - Default: `localhost:5432`

- **Kafka Broker** (required for event publishing)
  - Can use Docker Compose from parent project
  - Or standalone Kafka installation
  - Default: `localhost:9092`

### Optional

- **Docker** (for containerized deployment)
- **IDE** (IntelliJ IDEA, Eclipse, VS Code with Java extensions)

---

## Configuration

### Environment Variables

The service can be configured via environment variables or Spring profiles:

| Variable | Default | Description |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/postgres` | PostgreSQL connection URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | Kafka broker connection string |
| `SPRING_PROFILES_ACTIVE` | `default` | Active Spring profile (`default`, `docker`) |
| `SERVER_PORT` | `8082` | HTTP server port |

### Configuration Files

#### `application.yml` (Default)
- Database connection settings
- Kafka producer configuration
- JPA/Hibernate settings (ddl-auto: create-drop)
- Swagger/OpenAPI configuration
- Jackson configuration (reject unknown properties)

---

## Running the Application

### Local Development

**Prerequisites**: PostgreSQL and Kafka must be running (use Docker Compose from parent project)

```bash
# Start infrastructure (from joshua-devin-final directory)
cd ../joshua-devin-final
docker compose up -d database kafka

# Return to API directory
cd ../tsg-9.27-devinjosh-feedback-api

# Run the application
./mvnw spring-boot:run
```

**Windows PowerShell**:
```powershell
.\mvnw spring-boot:run
```

**Expected Output**:
```
Started TsgfeedbackapiApplication in X.XXX seconds
```

### Option 2: Using Docker Compose (Full Stack)

From the `joshua-devin-final` directory:

```bash
# Start all services including this API
docker compose --profile app up -d feedback-api

# View logs
docker compose --profile app logs -f feedback-api
```

### Verifying the Service is Running

1. **Check Health Endpoint**:
   ```bash
   curl http://localhost:8082/api/v1/health
   ```
   Expected: `OK`

2. **Check Swagger UI**:
   - Open http://localhost:8082/swagger-ui.html
   - Should display interactive API documentation

3. **Check Logs**:
   - Look for: `Started TsgfeedbackapiApplication`
   - No database or Kafka connection errors

---

## API Endpoints

### Base URL
`http://localhost:8082/api/v1`

### Endpoints

#### 1. Submit Feedback
- **Method**: `POST`
- **Path**: `/feedback`
- **Request Body**:
  ```json
  {
    "memberId": "m-123",
    "providerName": "Dr. Smith",
    "rating": 4,
    "comment": "Great experience."
  }
  ```
- **Success Response**: `201 Created`
  ```json
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "memberId": "m-123",
    "providerName": "Dr. Smith",
    "rating": 4,
    "comment": "Great experience.",
    "submittedAt": "2025-11-19T16:23:00Z"
  }
  ```
- **Error Response**: `400 Bad Request` (validation errors)
  ```json
  {
    "errors": [
      {"field": "rating", "message": "Rating must be between 1 and 5"}
    ]
  }
  ```

#### 2. Get Feedback by ID
- **Method**: `GET`
- **Path**: `/feedback/{id}`
- **Path Parameter**: `id` (UUID)
- **Success Response**: `200 OK` with feedback object
- **Error Response**: `404 Not Found` if feedback doesn't exist

#### 3. Get Feedback by Member ID
- **Method**: `GET`
- **Path**: `/feedback?memberId={memberId}`
- **Query Parameter**: `memberId` (String)
- **Success Response**: `200 OK` with array of feedback objects
- **Error Response**: `404 Not Found` if no feedback found for member

#### 4. Health Check
- **Method**: `GET`
- **Path**: `/health`
- **Response**: `200 OK` with body `"OK"`

### Interactive API Documentation

**Swagger UI**: http://localhost:8082/swagger-ui.html

- Browse all endpoints
- Test endpoints interactively
- View request/response schemas
- See validation rules

---

## Testing

### Running All Tests

```bash
./mvnw clean test
```

**Windows PowerShell**:
```powershell
.\mvnw clean test
```

**Expected Output**:
```
[INFO] Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### Test Breakdown

- **`FeedbackServiceTest`**: 14 tests
  - Happy path feedback creation
  - Validation tests (memberId, providerName, rating, comment)
  - Multiple validation errors
  - GET by ID (found and not found)
  - GET by memberId (found and not found)
  - Entity/DTO mapping verification

- **`FeedbackControllerTest`**: 4 tests
  - POST feedback happy path (201 Created)
  - POST with invalid JSON (400 Bad Request)
  - GET by ID happy path (200 OK)
  - GET by memberId happy path (200 OK)

### Running Specific Tests

```bash
# Run only service tests
./mvnw test -Dtest=FeedbackServiceTest

# Run only controller tests
./mvnw test -Dtest=FeedbackControllerTest

# Run a specific test method
./mvnw test -Dtest=FeedbackServiceTest#createFeedback_WithValidRequest_ReturnsResponse
```

### Test Coverage

- **Service Validation**: All validation rules tested (memberId, providerName, rating, comment)
- **Happy Path**: Successful feedback creation and retrieval
- **Error Handling**: Not found scenarios, validation errors
- **Controller Endpoints**: All REST endpoints tested
- **JSON Serialization**: Request/response serialization/deserialization
- **Exception Handling**: Global exception handler coverage

### Understanding Test Output

**Mockito Warnings** (Expected):
```
Mockito is currently self-attaching to enable the inline-mock-maker
```
These are informational warnings and don't affect test results.

---

### Useful Links

- **Swagger UI**: http://localhost:8082/swagger-ui.html
- **API Docs (JSON)**: http://localhost:8082/api-docs
- **Health Endpoint**: http://localhost:8082/api/v1/health
- **Kafka UI**: http://localhost:8000 (when running via Docker Compose)

### Additional Resources

#### Related Documentation

- **Main Project README**: `../joshua-devin-final/README.md`
- **Analytics Consumer**: `../tsg-9.27-devinjosh-feedback-analytics-consumer/README.md`
- **Project Specification**: `../joshua-devin-final/documentation/Spec_Provider_Feedback_Portal.md`
- **API Implementation Notes**: `docs/API_Implemenation.md`

#### Spring Boot Documentation

- [Spring Boot Reference](https://docs.spring.io/spring-boot/reference/)
- [Spring Data JPA](https://docs.spring.io/spring-data/jpa/reference/)
- [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/reference/)