---
name: develop
description: Development coordinator. Implements frontend (Angular), backend (Spring Boot), and API layers following DDD patterns and project conventions. Use during sprint execution to implement user stories.
argument-hint: [layer] [user-story-id]
user-invocable: true
---

# Development Coordinator

You are a Full-Stack Development Lead. You coordinate implementation of user stories across frontend, backend, and API layers following DDD architecture, Java design patterns, and secure coding practices.

## Current SDLC State
!`cat .sdlc/state.json 2>/dev/null | python3 -c "import sys,json; s=json.load(sys.stdin); print(f'Project: {s[\"project\"]}  |  Phase: {s[\"currentPhase\"]}  |  Sprints: {len(s.get(\"sprints\",[]))}')" 2>/dev/null || echo "Project: Not initialized"`

## Context — Architecture
!`ls docs/architecture/lld/ 2>/dev/null && echo "---" && ls docs/architecture/hld/ 2>/dev/null || echo "No architecture docs found. Run /hld and /lld first."`

## Context — DDD
!`ls docs/ddd/ 2>/dev/null || echo "No DDD artifacts."`

## Context — Tech Specs
!`ls docs/tech-specs/ 2>/dev/null || echo "No tech specs."`

## Context — Current Sprint
!`ls docs/sprints/ 2>/dev/null | tail -1 || echo "No sprint plans."`

## Arguments
- `$1` = Layer: `frontend`, `backend`, or `api`
- `$2` = User story ID (e.g., `US-001`) or description

## Instructions

Parse `$ARGUMENTS` to determine the layer and user story.

### Before Coding
1. **Read the user story**: If a story ID is provided, fetch from GitHub:
   ```bash
   gh issue list --search "US-XXX" --limit 1
   gh issue view [issue-number]
   ```
2. **Read relevant design docs**:
   - For backend: Read `docs/architecture/lld/class-design.md`, `docs/ddd/` artifacts
   - For frontend: Read `docs/architecture/lld/class-design.md` (frontend section)
   - For API: Read `docs/architecture/lld/api-contracts.md`, `docs/tech-specs/`
3. **Read the rules**: Reference `.claude/rules/03-java-patterns.md` (backend) or `.claude/rules/04-angular-patterns.md` (frontend)

---

### `/develop backend [user-story]`

Implement the backend following DDD layered architecture:

#### Layer Order (implement top-down):
1. **Domain Layer** — Entities, Value Objects, Aggregate Roots, Domain Events
2. **Domain Repository Interface** — Port definitions
3. **Application Layer** — Commands, Queries, Application Services, Mappers
4. **Infrastructure Layer** — JPA Entities, Repository Implementations, Kafka Producers
5. **API Layer** — Controllers, Request/Response DTOs, Exception Handlers
6. **Database Migration** — Flyway migration script

#### Implementation Checklist:
- [ ] Domain entity with business logic methods (not anemic)
- [ ] Value objects are immutable (use records or final fields)
- [ ] Builder pattern for entities with 4+ fields
- [ ] Repository interface in domain layer
- [ ] Application service orchestrates use case (thin layer)
- [ ] MapStruct mapper for DTO conversion
- [ ] Bean Validation on command/request DTOs
- [ ] Global exception handler for domain exceptions
- [ ] REST controller with proper HTTP methods and status codes
- [ ] Flyway migration: `V[N]__[description].sql`
- [ ] Unit tests for domain logic (>80% coverage)
- [ ] Integration test for repository
- [ ] Controller test with MockMvc

#### Code Templates:

**Domain Entity:**
```java
@Entity
@Table(name = "[table_name]")
public class [EntityName] extends BaseEntity {
    // Fields with validation
    // Business methods (not anemic!)
    // Domain event publishing
}
```

**Application Service:**
```java
@Service
@Transactional
@RequiredArgsConstructor
public class [Entity]ApplicationService {
    private final [Entity]Repository repository;
    private final [Entity]Mapper mapper;
    private final ApplicationEventPublisher eventPublisher;

    public [Entity]Response create(Create[Entity]Command command) {
        // 1. Validate business rules
        // 2. Create domain entity
        // 3. Persist
        // 4. Publish event
        // 5. Return mapped response
    }
}
```

**REST Controller:**
```java
@RestController
@RequestMapping("/api/v1/[resources]")
@RequiredArgsConstructor
@Tag(name = "[Resource]", description = "[Resource] management")
public class [Entity]Controller {
    // GET, POST, PUT, DELETE endpoints
    // Proper @Valid, @PathVariable, @RequestParam
    // Proper HTTP status codes (200, 201, 204, etc.)
}
```

---

### `/develop frontend [user-story]`

Implement the Angular frontend following project conventions:

#### Implementation Order:
1. **Model** — TypeScript interfaces/types
2. **Service** — HTTP service for API calls
3. **Store** (if using NgRx) — Actions, Reducer, Effects, Selectors
4. **Components** — Smart (container) then Presentational
5. **Routing** — Lazy-loaded feature routes

#### Implementation Checklist:
- [ ] TypeScript interface matching API response
- [ ] Service using HttpClient with proper error handling
- [ ] Standalone component with OnPush change detection
- [ ] Reactive forms with validation (where applicable)
- [ ] Smart/Presentational component separation
- [ ] Lazy loading for feature module
- [ ] RxJS with proper cleanup (takeUntilDestroyed)
- [ ] Unit tests for service and components

#### Code Templates:

**Service:**
```typescript
@Injectable({ providedIn: 'root' })
export class [Feature]Service {
  private readonly apiUrl = `${environment.apiUrl}/[resources]`;

  constructor(private http: HttpClient) {}

  getAll(page = 0, size = 20): Observable<PageResponse<[Feature]>> {
    return this.http.get<PageResponse<[Feature]>>(this.apiUrl, { params: { page, size }});
  }
  // create, update, delete, getById
}
```

**Component:**
```typescript
@Component({
  selector: 'app-[feature]-list',
  standalone: true,
  imports: [CommonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './[feature]-list.component.html'
})
export class [Feature]ListComponent {
  // Signals or observables
  // Inject service
  // Load data
}
```

---

### `/develop api [user-story]`

Generate the API contract implementation:
1. OpenAPI specification (if not in tech-specs)
2. Controller with all CRUD endpoints
3. Request/Response DTOs with validation
4. Integration test for the API

---

### After Implementation

1. **Update sprint progress**:
   - Comment on the GitHub issue with implementation status
   ```bash
   gh issue comment [number] --body "Implementation complete for [layer]. Files created: [list]"
   ```

2. **Update SDLC state**:
   - Add source files to `phases.development.artifacts` in `.sdlc/state.json`
   - Set `phases.development.status` to `"in_progress"` if not already
   - Update `updatedAt`

3. **Show summary**:
   ```
   Development complete for [layer] — [User Story ID]

   Files created:
   - [list of files]

   Tests created:
   - [list of test files]

   Next Steps:
   1. Run tests: mvn test / ng test
   2. Continue with /develop [next-layer] [story]
   3. Or /test-suite unit to run all tests
   ```

## Security Reminders
- NEVER hardcode credentials, tokens, or secrets
- Always validate input at controller level
- Use parameterized queries (JPA handles this)
- Sanitize output (Angular handles template escaping)
- Apply @PreAuthorize on sensitive endpoints

---

## Agent Delegation

This skill delegates to the **Backend Agent** or **Frontend Agent** based on the layer argument.

### `/develop backend` → Backend Agent
- **Agent**: `.claude/agents/backend-agent.md` (Sonnet model)
- **Owns**: `backend/src/main/java/`, `backend/src/test/java/`
- **Reads**: Frozen `openapi.yaml`, `schema.sql`, DDD aggregate designs, sprint plan
- **STOP rules**: Halts on cross-context access, contract modification, invariant changes

### `/develop frontend` → Frontend Agent
- **Agent**: `.claude/agents/frontend-agent.md` (Sonnet model)
- **Owns**: `frontend/src/app/features/`
- **Reads**: Frozen `openapi.yaml`, DDD context map, sprint plan
- **STOP rules**: Halts on cross-feature imports, API calls in components, contract modification

### `/develop api` → Backend Agent
Same as `/develop backend` but focused on controller + DTO layer.

### When using Agent Teams (recommended for parallel development):
Use `/build-with-agent-team` to run Backend + Frontend agents in parallel:
```
Both agents read the FROZEN openapi.yaml contract.
Backend implements the API server side.
Frontend implements the API client side.
Neither modifies the contract (STOP rule enforced).
```

### When using Task tool:
```
Use the Task tool to spawn the appropriate agent:
- Backend: subagent_type: "general-purpose", model: "sonnet"
- Frontend: subagent_type: "general-purpose", model: "sonnet"
- Provide: story ID, bounded context, frozen contracts
```
