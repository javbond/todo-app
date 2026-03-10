---
name: develop
description: Development coordinator. Implements frontend (Angular), backend (Spring Boot), and API layers following DDD patterns and project conventions. Use during sprint execution to implement user stories.
argument-hint: [layer] [user-story-id]
user-invocable: true
---

# Development Coordinator

You are a Full-Stack Development Lead. You coordinate implementation of user stories across frontend, backend, and API layers following DDD architecture, Java design patterns, and secure coding practices.

## Current SDLC State
!`python3 -c 'import json; s=json.load(open(".sdlc/state.json")); print("Project: " + s.get("project","?") + "  |  Phase: " + s.get("currentPhase","?") + "  |  Sprints: " + str(len(s.get("sprints",[]))))' 2>/dev/null || echo "Project: Not initialized"`

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
0. **Sync and prepare branch** (GitHub Flow):
   ```bash
   # Ensure main is up to date
   git fetch origin
   git checkout main
   git pull origin main

   # Determine feature branch name from user story
   # Convention: feature/US-XXX-short-desc (from 01-general.md)
   BRANCH="feature/$2"

   # Check if feature branch exists
   if git show-ref --verify --quiet "refs/heads/$BRANCH"; then
     # Branch exists — switch to it and merge latest main
     git checkout "$BRANCH"
     git merge main
   else
     # Create new feature branch from main
     git checkout -b "$BRANCH" main
   fi

   # Verify clean working directory
   if [ -n "$(git status --porcelain)" ]; then
     echo "WARNING: Uncommitted changes detected. Stash or commit before proceeding."
   fi
   ```
   If the working directory is not clean, **STOP** and ask the user whether to stash or commit the changes before proceeding.

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


---

### `/develop workspace [workspace-name] [user-story]`

Implement code in an **additional workspace** (non-primary tech stack).

#### Step 1: Resolve Workspace Config
Read `.sdlc/state.json` → `techStack.additional` to find the matching workspace:
```bash
python3 -c "
import json
with open('.sdlc/state.json') as f:
    state = json.load(f)
for ws in state.get('techStack', {}).get('additional', []):
    if ws.get('name','').lower().replace(' ','-') == '[workspace-name]'.lower():
        print(f'Directory: {ws["directory"]}')
        print(f'Technology: {ws["technology"]} {ws["version"]}')
        print(f'Language: {ws["language"]}')
        print(f'Build: {ws["buildCmd"]}')
        print(f'Test: {ws["testCmd"]}')
        print(f'Reference: {ws.get("referenceDoc", "none")}')
"
```

#### Step 2: Read Reference Documentation
If the workspace has a `referenceDoc` in state.json, READ it thoroughly before implementing:
- Reference docs in `docs/tech-refs/<workspace>/` contain architecture patterns, coding conventions, and implementation guidance
- These docs are REFERENCE material — use them to understand the workspace's patterns but ensure completeness by consulting the project's HLD/LLD/DDD artifacts

#### Step 3: Implement
Follow the workspace's technology conventions (from reference docs and 06-tech-stack-context.md).
- Use the workspace's build command to verify: `[buildCmd]`
- Use the workspace's test command to verify: `[testCmd]`
- Respect the workspace's language idioms and patterns

#### Step 4: Integration Points
Check `docs/tech-specs/shared-schemas/` for cross-workspace contracts (gRPC protos, OpenAPI specs, Kafka topic schemas).
Ensure the workspace implementation adheres to these shared contracts.

### After Implementation

0. **Commit and push work**:
   - Stage the files created during this implementation (do NOT use `git add -A` which may include secrets or build artifacts)
   ```bash
   # Stage specific files created/modified
   git add [list of files created]

   # Commit with conventional commit format
   git commit -m "feat(bounded-context): implement [User Story ID] — [short description]"

   # Push feature branch to remote
   git push -u origin "$(git branch --show-current)"
   ```
   Ensure the commit message follows conventional commits format from `.claude/rules/01-general.md`.

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


### `/develop workspace <name>` → Dynamic Agent
- Reads `state.json → techStack.additional` for workspace config
- If workspace technology has a matching agent (e.g., backend-agent for Spring Boot), use it
- Otherwise, use a general-purpose agent with the workspace's reference docs as context
- Agent is given: workspace directory, build/test commands, reference doc path, shared contracts

### Multi-Stack Awareness
Read `.claude/rules/06-tech-stack-context.md` for the full project tech stack.
When implementing in any workspace, be aware of integration points with other workspaces.
Check `docs/tech-specs/shared-schemas/` for cross-workspace contracts.

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
