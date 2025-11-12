# Work Split for Feedback API

#### Devin: Service Layer and validation (business logic)
Focus: DTOs, validation service, and tests

Files to create/modify:
1. `dtos/FeedbackRequest.java` — request DTO with validation annotations
2. `dtos/FeedbackResponse.java` — response DTO
3. `dtos/ErrorResponse.java` — error response structure
4. `services/ValidationException.java` — custom exception
5. `services/FeedbackService.java` — service with validation logic
6. `src/test/java/.../services/FeedbackServiceTest.java` — unit tests
7. `src/main/resources/application-local.yml` — local config
8. `src/main/resources/application-docker.yml` — Docker config

Dependencies:
- Can work independently
- Will mock `FeedbackRepository` and `FeedbackEventPublisher` in tests
- Needs entity structure (coordinate field names with Devin)

#### Josh: Infrastructure and integration (data and messaging)
Focus: Database, Kafka, and REST endpoints

Files to create/modify:
1. `repositories/entities/FeedbackEntity.java` — JPA entity
2. `repositories/FeedbackRepository.java` — Spring Data JPA repository
3. `messaging/KafkaProducerConfig.java` — Kafka producer configuration
4. `messaging/FeedbackEventPublisher.java` — event publisher service
5. `controllers/FeedbackController.java` — REST controller
6. Database migration (Flyway) or schema init script
7. `src/test/java/.../controllers/FeedbackControllerTest.java` — MockMvc tests (optional for Day 3)

Dependencies:
- Needs DTOs from Josh (can stub initially)
- Needs service interface (can stub initially)
- Will implement the entity first


### Phase 1: Parallel setup (start together, ~30 min)
1. Devin: Create DTO package structure and stub classes
2. Josh: Create entity package structure and define `FeedbackEntity` with fields
3. Sync: Share entity field names (memberId, providerName, rating, comment, submittedAt)

### Phase 2: Parallel development (main work)
Devin:
- Implement DTOs with Jackson annotations
- Implement `ValidationException`
- Implement `FeedbackService` with all validation rules
- Write unit tests for validation
- Create `application-local.yml` and `application-docker.yml`

Josh:
- Complete `FeedbackEntity` with JPA annotations
- Create `FeedbackRepository` interface
- Implement `KafkaProducerConfig`
- Implement `FeedbackEventPublisher`
- Create database schema (Flyway migration or JPA auto-create)
- Stub `FeedbackController` (can wire to service later)

### Phase 3: Integration (after both complete)
1. Josh: Wire `FeedbackController` to `FeedbackService`
2. Devin: Wire `FeedbackService` to `FeedbackRepository` and `FeedbackEventPublisher`
3. Both: Test end-to-end flow

---


## Coordination points

1. Entity field names: Josh defines the entity first; Devin uses matching field names in DTOs
2. Service interface: Josh defines the service signature; Devin wires it in the controller
3. Kafka event contract: Use the same structure as the consumer (already defined)
4. Package name: The spec says `com.tsg.feedbackapi`, but the current code uses `net.yorksolutions.tsgfeedbackapi`. Decide which to use.

---

## Suggested communication protocol

1. Start: Josh shares entity field names → Devin uses them in DTOs
2. Midway: Josh shares service method signatures → Devin wires controller
3. End: Both test integration together

---

## Day 3 completion checklist

Devin:
- [ ] DTOs created with proper validation
- [ ] `FeedbackService` with all validation rules
- [ ] Unit tests for validation (100% coverage of validation logic)
- [ ] Config files created (`application-local.yml`, `application-docker.yml`)

Josh:
- [ ] `FeedbackEntity` with JPA annotations
- [ ] `FeedbackRepository` interface
- [ ] `KafkaProducerConfig` configured
- [ ] `FeedbackEventPublisher` implemented
- [ ] Database schema created
- [ ] `FeedbackController` stubbed (can be fully wired in Phase 3)