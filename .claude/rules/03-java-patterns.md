# Java & Spring Boot Conventions

## DDD Layered Architecture

```
bounded-context-module/
├── domain/
│   ├── model/           # Entities, Value Objects, Aggregates
│   ├── event/           # Domain Events
│   ├── exception/       # Domain-specific exceptions
│   ├── repository/      # Repository interfaces (ports)
│   └── service/         # Domain services
├── application/
│   ├── command/         # Command DTOs (write operations)
│   ├── query/           # Query DTOs (read operations)
│   ├── service/         # Application services (use case orchestration)
│   └── mapper/          # DTO <-> Domain mappers (MapStruct)
├── infrastructure/
│   ├── persistence/     # JPA entities, Repository implementations (adapters)
│   ├── messaging/       # Kafka producers/consumers
│   ├── external/        # External API clients
│   └── config/          # Spring configuration
└── api/
    ├── controller/      # REST controllers
    ├── dto/             # API request/response DTOs
    └── advice/          # Exception handlers (@ControllerAdvice)
```

## Design Patterns (Required Usage)

| Pattern | When to Use | Example |
|---------|------------|---------|
| **Builder** | Objects with 4+ constructor params | `User.builder().name("x").email("y").build()` |
| **Factory** | Complex object creation logic | `OrderFactory.createFromCart(cart)` |
| **Strategy** | Multiple algorithms for same task | `PricingStrategy` with discount variants |
| **Repository** | Data access abstraction | `UserRepository` interface in domain layer |
| **Specification** | Complex query criteria | `UserSpecification.hasRole(ADMIN)` |
| **Observer/Events** | Cross-context communication | `OrderPlacedEvent` → `InventoryService` |
| **Template Method** | Common workflow with varying steps | `AbstractApprovalProcessor` |

## Spring Boot Best Practices

### DTOs and Mapping
- Never expose domain entities in API responses
- Use MapStruct for DTO mapping: `@Mapper(componentModel = "spring")`
- Separate request DTOs (Create/Update) from response DTOs
- Use Java records for immutable DTOs

### Validation
- Use Bean Validation annotations (`@NotNull`, `@Size`, `@Email`, etc.)
- Create custom validators for business rules
- Validate at controller layer, enforce at domain layer

### Exception Handling
```java
// Global exception handler
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)  // → 404
    @ExceptionHandler(BusinessRuleException.class)    // → 422
    @ExceptionHandler(ValidationException.class)      // → 400
}
```

### Service Layer
- Application services are thin — orchestrate, don't implement business logic
- Domain services contain business logic that doesn't belong to a single entity
- Use `@Transactional` at application service level, not repository

### Repository Layer
- Define interfaces in `domain/repository/` (port)
- Implement in `infrastructure/persistence/` (adapter)
- Use Spring Data JPA for CRUD, custom queries for complex operations
- Use `@Query` with JPQL for type safety

### Configuration
- Use `application.yml` (not .properties)
- Externalize all configs (no hardcoded values)
- Use Spring profiles: `dev`, `staging`, `prod`
- Use `@ConfigurationProperties` for typed config binding

## Database Conventions
- Table names: `snake_case`, plural (`users`, `order_items`)
- Column names: `snake_case` (`created_at`, `user_id`)
- Primary keys: `UUID` (not auto-increment for distributed systems)
- Always include audit columns: `created_at`, `updated_at`, `created_by`, `updated_by`
- Soft delete with `is_deleted` flag (not physical delete)
- Use Flyway for migrations: `V1__create_users_table.sql`
