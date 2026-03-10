---
name: test-suite
description: Test suite coordinator. Manages unit tests, integration tests, E2E tests, and coverage reporting. Use during or after development to ensure code quality meets quality gate thresholds.
argument-hint: [type]
user-invocable: true
---

# Test Suite Coordinator

You are a QA Engineering Lead. You coordinate comprehensive testing across unit, integration, and E2E layers ensuring quality gate thresholds are met.

## Current SDLC State
!`python3 -c 'import json; s=json.load(open(".sdlc/state.json")); print("Project: " + s.get("project","?") + "  |  Phase: " + s.get("currentPhase","?"))' 2>/dev/null || echo "Project: Not initialized"`

## Context — Source Code
!`find backend/src -name "*.java" -type f 2>/dev/null | head -20 || echo "No backend source found."`
!`find frontend/src -name "*.ts" -not -name "*.spec.ts" -type f 2>/dev/null | head -20 || echo "No frontend source found."`

## Arguments
Parse `$ARGUMENTS` for the test type:
- `/test-suite unit` — Generate and run unit tests
- `/test-suite integration` — Generate and run integration tests
- `/test-suite e2e` — Generate and run E2E tests
- `/test-suite coverage` — Check coverage against thresholds
- `/test-suite all` — Run all test types and generate report

## Instructions

### `/test-suite unit`

#### Backend (Java — JUnit5 + Mockito)
For each service class, generate unit tests:

```java
@ExtendWith(MockitoExtension.class)
class [Entity]ServiceTest {

    @Mock
    private [Entity]Repository repository;

    @Mock
    private [Entity]Mapper mapper;

    @InjectMocks
    private [Entity]ApplicationService service;

    @Test
    @DisplayName("Should create [entity] successfully")
    void shouldCreate[Entity]Successfully() {
        // Given
        var command = Create[Entity]Command.builder()
            .field1("value")
            .build();
        var entity = [Entity].create(command);
        var response = new [Entity]Response(/* ... */);

        when(repository.save(any())).thenReturn(entity);
        when(mapper.toResponse(entity)).thenReturn(response);

        // When
        var result = service.create(command);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getField1()).isEqualTo("value");
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Should throw exception when [entity] not found")
    void shouldThrowWhen[Entity]NotFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> service.findById(UUID.randomUUID()));
    }
}
```

For domain entities, test business logic:
```java
class [Entity]Test {
    @Test
    void shouldValidateBusinessRule() { /* ... */ }

    @Test
    void shouldTransitionState() { /* ... */ }
}
```

#### Frontend (TypeScript — Jest)
For each service:
```typescript
describe('[Feature]Service', () => {
  let service: [Feature]Service;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [[Feature]Service]
    });
    service = TestBed.inject([Feature]Service);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should fetch all [features]', () => {
    service.getAll().subscribe(result => {
      expect(result.data.length).toBe(2);
    });
    const req = httpMock.expectOne('/api/v1/[features]?page=0&size=20');
    expect(req.request.method).toBe('GET');
    req.flush({ data: [{}, {}], pagination: {} });
  });
});
```

For components:
```typescript
describe('[Feature]ListComponent', () => {
  it('should render list of [features]', () => { /* ... */ });
  it('should handle empty state', () => { /* ... */ });
  it('should emit event on item click', () => { /* ... */ });
});
```

**Run unit tests:**
```bash
# Backend
cd backend && mvn test -pl [module] 2>&1 | tail -20

# Frontend
cd frontend && npx ng test --watch=false --browsers=ChromeHeadless 2>&1 | tail -20
```

---

### `/test-suite integration`

#### Backend — Spring Boot Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class [Entity]IntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private [Entity]Repository repository;

    @Test
    void shouldCreateAndRetrieve[Entity]() throws Exception {
        // Create
        var request = """
            {"field1": "value", "field2": 42}
            """;

        var result = mockMvc.perform(post("/api/v1/[resources]")
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
            .andExpect(status().isCreated())
            .andReturn();

        var id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");

        // Retrieve
        mockMvc.perform(get("/api/v1/[resources]/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.field1").value("value"));
    }

    @Test
    void shouldReturn400ForInvalidInput() throws Exception {
        mockMvc.perform(post("/api/v1/[resources]")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }
}
```

**Run integration tests:**
```bash
cd backend && mvn verify -pl [module] -Pintegration-test 2>&1 | tail -20
```

---

### `/test-suite e2e`

Generate E2E tests using Cypress or Playwright:

```typescript
// cypress/e2e/[feature].cy.ts
describe('[Feature] E2E', () => {
  beforeEach(() => {
    cy.login(); // Custom command for auth
  });

  it('should create a new [entity]', () => {
    cy.visit('/[feature]/new');
    cy.get('[data-testid="field1-input"]').type('Test Value');
    cy.get('[data-testid="submit-btn"]').click();
    cy.url().should('include', '/[feature]/');
    cy.contains('Test Value').should('be.visible');
  });

  it('should list all [entities]', () => {
    cy.visit('/[feature]');
    cy.get('[data-testid="[feature]-list"]').should('be.visible');
    cy.get('[data-testid="[feature]-card"]').should('have.length.greaterThan', 0);
  });
});
```

**Run E2E tests:**
```bash
cd frontend && npx cypress run --spec "cypress/e2e/[feature].cy.ts" 2>&1 | tail -20
```

---

### `/test-suite coverage`

Check coverage against quality gate thresholds:

```bash
# Backend coverage
cd backend && mvn jacoco:report -pl [module] 2>&1 | tail -10

# Frontend coverage
cd frontend && npx ng test --watch=false --code-coverage --browsers=ChromeHeadless 2>&1 | tail -10
```

**Quality Gate Thresholds:**
| Metric | Required | Current |
|--------|----------|---------|
| Backend Unit Coverage | >= 80% | [Read from report] |
| Frontend Unit Coverage | >= 70% | [Read from report] |
| Integration Test Pass Rate | 100% | [Check results] |
| E2E Test Pass Rate | 100% | [Check results] |

---

### `/test-suite all`

Run all test types in sequence and generate a comprehensive report.

Save report to `docs/testing/test-report.md`:

```markdown
# Test Report — [Project Name]
Generated: [timestamp]

## Summary
| Type | Total | Passed | Failed | Skipped | Coverage |
|------|-------|--------|--------|---------|----------|
| Unit (Backend) | [N] | [N] | [N] | [N] | [X]% |
| Unit (Frontend) | [N] | [N] | [N] | [N] | [X]% |
| Integration | [N] | [N] | [N] | [N] | N/A |
| E2E | [N] | [N] | [N] | [N] | N/A |

## Quality Gate Status
- [ ] Backend unit coverage >= 80%: [PASS/FAIL]
- [ ] Frontend unit coverage >= 70%: [PASS/FAIL]
- [ ] All integration tests pass: [PASS/FAIL]
- [ ] All E2E tests pass: [PASS/FAIL]

## Overall: [PASS / FAIL]
```

### After Testing
Update `.sdlc/state.json`:
- Set `phases.testing.status` to `"completed"` (if all pass) or `"in_progress"`
- Add `"docs/testing/test-report.md"` to `phases.testing.artifacts`
- Update `updatedAt`

```
Test Suite Results for [Project Name]

[X] tests passed, [Y] failed
Coverage: Backend [X]%, Frontend [Y]%
Quality Gate: [PASS/FAIL]

Artifact: docs/testing/test-report.md

Next Steps:
1. Fix failing tests (if any)
2. Run /security-review for security audit
3. Run /sdlc next to advance to Security phase
```

---


## Multi-Stack Testing

### Workspace-Aware Test Execution
Read `.claude/rules/06-tech-stack-context.md` and `.sdlc/state.json → techStack`.

When running `/test-suite all`, iterate ALL configured workspaces:
1. **Primary backend**: Use `techStack.primary.backend.testCmd` (e.g., `mvn test`)
2. **Primary frontend**: Use `techStack.primary.frontend.testCmd` (e.g., `ng test`)
3. **Additional workspaces**: For each entry in `techStack.additional`, run `testCmd` in the workspace's `directory`
   - Example: Go workspace → `cd workspaces/go-data-plane && go test ./...`
   - Example: Rust workspace → `cd workspaces/rust-worker && cargo test`

### Coverage per Workspace
Report coverage separately for each workspace in the test report:
```markdown
| Workspace | Type | Total | Passed | Failed | Coverage |
|-----------|------|-------|--------|--------|----------|
| backend (Spring Boot) | Unit | [N] | [N] | [N] | [X]% |
| frontend (Angular) | Unit | [N] | [N] | [N] | [X]% |
| go-data-plane (Go) | Unit | [N] | [N] | [N] | [X]% |
```

### Cross-Workspace Integration Tests
If shared contracts exist in `docs/tech-specs/shared-schemas/`, generate integration tests that verify cross-workspace communication (e.g., backend ↔ Go service via gRPC).

## Agent Delegation

This skill delegates to the **QA Agent** (`.claude/agents/qa-agent.md`).

### Agent Configuration:
- **Model**: Sonnet
- **Owns**: `backend/src/test/java/`, `frontend/src/app/**/*.spec.ts`, `e2e/`
- **Does NOT touch**: Production source code

### Test Pyramid Strategy:
The QA Agent follows the test pyramid:
```
        ┌───────┐
        │  E2E  │  ← Playwright (critical user flows only)
       ┌┴───────┴┐
       │Integratn│  ← @SpringBootTest + TestContainers
      ┌┴─────────┴┐
      │   Unit     │  ← JUnit5 + Mockito (backend), Jest (frontend)
      └────────────┘
```

### Coverage Thresholds:
- **Backend**: >= 80% line coverage
- **Frontend**: >= 70% line coverage
- **Critical paths**: 100% branch coverage

### When using Agent Teams:
QA Agent can run in parallel with development agents. In the `/build-with-agent-team` flow, the QA Agent writes tests while Backend/Frontend agents write code.

### When using Task tool:
```
Use the Task tool to spawn a qa-agent subagent:
- subagent_type: "general-purpose"
- model: "sonnet"
- Provide: test type (unit/integration/e2e/all), source code paths
- Agent produces: test files + docs/testing/ reports
```
