---
name: uat-setup
description: UAT environment setup and acceptance testing. Spins up the full stack (database, backend, frontend), generates test cases from acceptance criteria, collects pass/fail results, and produces UAT reports. Use after automated tests pass and before security review.
argument-hint: [action] [sprint]
user-invocable: true
---

# User Acceptance Testing (UAT)

You are a UAT Coordinator. You set up running environments for manual stakeholder testing, generate test cases from user story acceptance criteria, collect results, and produce sign-off reports.

## Current SDLC State
!`python3 -c 'import json; s=json.load(open(".sdlc/state.json")); print("Project: " + s.get("project","?") + "  |  Phase: " + s.get("currentPhase","?") + "  |  Sprints: " + str(len(s.get("sprints",[]))))' 2>/dev/null || echo "Project: Not initialized"`

## Context — Current Sprint
!`python3 -c 'import json; s=json.load(open(".sdlc/state.json")); sprints=s.get("sprints",[]); cur=[sp for sp in sprints if sp.get("status")=="in_progress"]; print("Sprint: " + cur[0]["name"] + "  |  Stories: " + str(len(cur[0].get("stories",[]))) if cur else "No active sprint")' 2>/dev/null || echo "No sprint info"`

## Context — Environment Status
!`docker compose ps --format "table {{.Name}}\t{{.Status}}" 2>/dev/null || echo "No containers running"`

## Arguments
- `/uat-setup start [sprint]` — Spin up full environment (DB + backend + frontend), seed data, verify health
- `/uat-setup test-cases [sprint]` — Generate UAT test cases from user story acceptance criteria
- `/uat-setup report [sprint]` — Collect pass/fail results and generate UAT sign-off report
- `/uat-setup teardown` — Stop all services and clean up containers

---

### `/uat-setup start [sprint]`

Set up a fully running UAT environment for manual frontend testing.

#### Step 0: Read Project Context
```bash
# Read tech stack and sprint info
cat .sdlc/state.json
```
Identify from state.json:
- `techStack.primary.backend` → backend technology, directory, build/run commands
- `techStack.primary.frontend` → frontend technology, directory, serve command
- `techStack.primary.database` → database type (PostgreSQL, MySQL, etc.)
- `techStack.primary.cache` → cache type (Redis, etc.)
- `techStack.primary.messaging` → messaging type (Kafka, RabbitMQ, etc.)
- Current sprint name and stories

Also read:
- `docs/architecture/hld/` for infrastructure dependencies
- `docs/tech-specs/` for database schemas and API contracts

#### Step 1: Infrastructure — Docker Compose

**Check if `docker-compose.yml` exists:**
```bash
ls docker-compose.yml 2>/dev/null || ls docker-compose.yaml 2>/dev/null || echo "NOT_FOUND"
```

**If NOT found**, generate one based on the tech stack:

```yaml
# docker-compose.yml — UAT Environment
version: '3.8'

services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_DB: ${DB_NAME:-app_db}
      POSTGRES_USER: ${DB_USER:-app_user}
      POSTGRES_PASSWORD: ${DB_PASSWORD:-app_secret}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./infrastructure/init-db:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${DB_USER:-app_user}"]
      interval: 5s
      timeout: 5s
      retries: 5

  # Include Redis if techStack.primary.cache exists
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 5s
      retries: 5

  # Include RabbitMQ if techStack.primary.messaging == "RabbitMQ"
  # Include Kafka if techStack.primary.messaging == "Kafka"

volumes:
  postgres_data:
```

**Adapt the template:**
- Only include services that match the project's tech stack
- If `techStack.primary.database` is MySQL, use mysql:8 image instead
- If no cache configured, omit Redis
- If no messaging configured, omit RabbitMQ/Kafka

Also generate `.env.example` if it doesn't exist:
```env
DB_NAME=app_db
DB_USER=app_user
DB_PASSWORD=app_secret
```

And `infrastructure/init-db/01-create-database.sql` if it doesn't exist:
```sql
-- Database initialization for UAT
-- Tables are created by Flyway migrations on backend startup
SELECT 'Database ready for Flyway migrations' AS status;
```

**If docker-compose.yml ALREADY exists**, use it as-is.

#### Step 2: Start Infrastructure
```bash
# Copy .env if needed
cp .env.example .env 2>/dev/null || true

# Start infrastructure containers
docker compose up -d

# Wait for health checks
echo "Waiting for infrastructure to be healthy..."
docker compose ps --format "table {{.Name}}\t{{.Status}}"

# Verify PostgreSQL is ready
until docker compose exec -T postgres pg_isready -U ${DB_USER:-app_user} 2>/dev/null; do
  sleep 2
done
echo "PostgreSQL is ready"

# Verify Redis is ready (if running)
docker compose exec -T redis redis-cli ping 2>/dev/null && echo "Redis is ready" || true
```

If Docker is NOT available on the system, fall back to manual mode:
```
Docker is not installed. For UAT, please ensure these services are running locally:
- PostgreSQL on port 5432
- Redis on port 6379 (if applicable)

Or install Docker: https://docs.docker.com/get-docker/
```

#### Step 3: Seed Test Data

Check for existing seed data:
```bash
# Check for Flyway migrations (will run on backend startup)
ls backend/src/main/resources/db/migration/*.sql 2>/dev/null | head -5

# Check for manual seed scripts
ls infrastructure/init-db/*.sql 2>/dev/null | head -5
ls infrastructure/seed-data/*.sql 2>/dev/null | head -5
```

If no seed data exists, generate a basic seed script based on the domain model:
1. Read `docs/ddd/` or `docs/architecture/lld/` for entity definitions
2. Read `docs/tech-specs/` for database schemas
3. Generate `infrastructure/seed-data/uat-seed-data.sql` with:
   - Sample records for each entity (5-10 per table)
   - Realistic test data that covers acceptance criteria scenarios
   - Test user accounts with known credentials

Run seed data:
```bash
# Flyway handles schema creation on backend startup
# Run additional seed data after backend is up
docker compose exec -T postgres psql -U ${DB_USER:-app_user} -d ${DB_NAME:-app_db} -f /seed-data/uat-seed-data.sql 2>/dev/null || true
```

#### Step 4: Start Backend
```bash
# Read build/run commands from state.json or use defaults
cd backend

# Build (skip tests — already passed in testing phase)
mvn clean package -DskipTests -q 2>&1 | tail -5

# Start in background
nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > /tmp/backend-uat.log 2>&1 &
BACKEND_PID=$!
echo "Backend starting (PID: $BACKEND_PID)..."

# Wait for actuator health
echo "Waiting for backend health check..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "Backend is UP on http://localhost:8080"
    break
  fi
  sleep 3
done

cd ..
```

For non-Spring Boot backends, read `techStack.primary.backend.runCmd` from state.json.

#### Step 5: Start Frontend
```bash
cd frontend

# Install dependencies if needed
npm install --prefer-offline 2>&1 | tail -3

# Start in background
nohup ng serve --open=false > /tmp/frontend-uat.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend starting (PID: $FRONTEND_PID)..."

# Wait for frontend to be ready
echo "Waiting for frontend..."
for i in $(seq 1 30); do
  if curl -sf http://localhost:4200 > /dev/null 2>&1; then
    echo "Frontend is UP on http://localhost:4200"
    break
  fi
  sleep 3
done

cd ..
```

For non-Angular frontends, read `techStack.primary.frontend.serveCmd` from state.json.

#### Step 6: Health Verification Summary

Run a final health check on all services and print the UAT environment summary:

```bash
echo ""
echo "============================================"
echo "  UAT ENVIRONMENT READY"
echo "============================================"
echo ""
echo "  Services:"
echo "  -----------------------------------------"
# Check each service
curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1 && echo "  Backend:    http://localhost:8080  ✅ UP" || echo "  Backend:    http://localhost:8080  ❌ DOWN"
curl -sf http://localhost:4200 > /dev/null 2>&1 && echo "  Frontend:   http://localhost:4200  ✅ UP" || echo "  Frontend:   http://localhost:4200  ❌ DOWN"
docker compose exec -T postgres pg_isready > /dev/null 2>&1 && echo "  PostgreSQL: localhost:5432        ✅ UP" || echo "  PostgreSQL: localhost:5432        ❌ DOWN"
docker compose exec -T redis redis-cli ping > /dev/null 2>&1 && echo "  Redis:      localhost:6379        ✅ UP" || echo "  Redis:      localhost:6379        ❌ DOWN"
echo ""
echo "  Frontend URL: http://localhost:4200"
echo "  API Base URL: http://localhost:8080/api"
echo ""
echo "  Logs:"
echo "  - Backend:  tail -f /tmp/backend-uat.log"
echo "  - Frontend: tail -f /tmp/frontend-uat.log"
echo ""
echo "  Next steps:"
echo "  1. Open http://localhost:4200 in your browser"
echo "  2. Run /uat-setup test-cases [sprint] to see what to test"
echo "  3. Run /uat-setup report [sprint] when testing is complete"
echo "  4. Run /uat-setup teardown when done"
echo "============================================"
```

If ANY critical service is DOWN, **STOP** and troubleshoot:
- Check logs: `tail -50 /tmp/backend-uat.log` or `tail -50 /tmp/frontend-uat.log`
- Check Docker: `docker compose logs --tail 50`
- Report the issue to the user before proceeding

#### Step 7: Update SDLC State
Update `.sdlc/state.json`:
- Set `phases.uat.status` to `"in_progress"`
- Set `phases.uat.startedAt` to current timestamp
- Add `phases.uat.environment` with service URLs and PIDs
- Update `updatedAt`

---

### `/uat-setup test-cases [sprint]`

Generate UAT test cases from user story acceptance criteria.

#### Step 1: Identify Sprint Stories
```bash
# Get sprint milestone name
SPRINT_NAME="Sprint $2"

# List stories in this sprint from GitHub
gh issue list --milestone "$SPRINT_NAME" --state all --json number,title,body,labels --limit 50
```

If GitHub issues aren't available, read from:
- `docs/sprints/sprint-$2-plan.md`
- `docs/prd/backlog.md`

#### Step 2: Extract Acceptance Criteria

For each user story, extract the acceptance criteria section. Typical format:
```
### Acceptance Criteria
- [ ] Given [precondition], When [action], Then [expected result]
- [ ] Given [precondition], When [action], Then [expected result]
```

If acceptance criteria are not in Given-When-Then format, convert them.

#### Step 3: Generate UAT Test Cases

Create `docs/uat/sprint-N-uat-test-cases.md`:

```markdown
# UAT Test Cases — Sprint N

## Overview
- **Sprint**: Sprint N
- **Generated**: [timestamp]
- **Total Test Cases**: [count]
- **Stories Covered**: [count]

---

## Test Case Index

| ID | Story | Test Case | Priority | Status |
|----|-------|-----------|----------|--------|
| UAT-001 | US-XXX | [short description] | Critical | ⬜ Pending |
| UAT-002 | US-XXX | [short description] | High | ⬜ Pending |
| ... | ... | ... | ... | ... |

---

## Detailed Test Cases

### UAT-001: [Test Case Title]
**Story**: US-XXX — [Story Title]
**Priority**: Critical
**Preconditions**: [What must be true before testing]

| Step | Action | Expected Result |
|------|--------|----------------|
| 1 | Navigate to [URL/page] | [Page loads successfully] |
| 2 | [User action] | [Expected UI response] |
| 3 | [User action] | [Expected data/behavior] |

**Test Data**: [Any specific data needed]
**Result**: ⬜ Pass / ⬜ Fail / ⬜ Blocked
**Notes**: _[tester fills in]_

---
[Repeat for each test case]
```

#### Step 4: Display Summary
Print:
- Total test cases generated
- Breakdown by priority (Critical / High / Medium / Low)
- Estimated testing time (2-5 min per test case)
- Remind user to open `http://localhost:4200` to start testing

---

### `/uat-setup report [sprint]`

Collect test results and generate the UAT sign-off report.

#### Step 1: Read Test Cases
Read `docs/uat/sprint-N-uat-test-cases.md` to get the list of test cases.

#### Step 2: Collect Results Interactively

For each test case (or group by story), ask the user using AskUserQuestion:

```
Test Case UAT-001: [description]
Story: US-XXX — [title]

What was the result?
- Pass — Works as expected
- Fail — Does not meet acceptance criteria
- Blocked — Cannot test (environment issue, dependency)
- Skip — Not applicable for this sprint
```

Collect notes for any Fail or Blocked results.

#### Step 3: Generate UAT Report

Create `docs/uat/sprint-N-uat-report.md`:

```markdown
# UAT Report — Sprint N

## Summary
- **Sprint**: Sprint N
- **Test Date**: [timestamp]
- **Tested By**: [stakeholder name or "Manual UAT"]
- **Environment**: http://localhost:4200 (frontend), http://localhost:8080 (backend)

## Results Overview

| Metric | Count |
|--------|-------|
| Total Test Cases | X |
| ✅ Passed | X |
| ❌ Failed | X |
| ⛔ Blocked | X |
| ⏭️ Skipped | X |
| **Pass Rate** | **X%** |

## Verdict

**[PASS / FAIL]** — [Summary statement]

Criteria for PASS: All Critical and High priority test cases pass. No more than 2 Medium failures.

---

## Detailed Results

### US-XXX — [Story Title]

| Test Case | Priority | Result | Notes |
|-----------|----------|--------|-------|
| UAT-001 | Critical | ✅ Pass | — |
| UAT-002 | High | ❌ Fail | [failure description] |

[Repeat for each story]

---

## Failed Items — Action Required

### UAT-002: [Test Case Title]
- **Story**: US-XXX
- **Priority**: High
- **Expected**: [what should happen]
- **Actual**: [what happened]
- **Action**: [Fix in current sprint / Log as backlog item]
- **GitHub Issue**: [created if logged]

---

## Sign-Off

| Role | Name | Decision | Date |
|------|------|----------|------|
| Product Owner | _______________ | ⬜ Approved / ⬜ Rejected | ________ |
| QA Lead | _______________ | ⬜ Approved / ⬜ Rejected | ________ |
| Tech Lead | _______________ | ⬜ Approved / ⬜ Rejected | ________ |

---

_Report generated by AI-Native SDLC Factory — `/uat-setup report`_
```

#### Step 4: Handle Failures

For any FAILED test cases:
1. Ask the user: "Should this be fixed in the current sprint or logged as a backlog item?"
2. If **fix now**: Do NOT mark UAT as complete — the developer should fix and re-test
3. If **backlog**: Create a GitHub issue with label `bug` and `uat-failure`:
   ```bash
   gh issue create --title "[UAT-FAIL] UAT-XXX: [description]" \
     --body "Failed during Sprint N UAT testing.\n\n**Expected:** ...\n**Actual:** ...\n\n**Priority:** [from test case]" \
     --label "bug,uat-failure"
   ```

#### Step 5: Update SDLC State

If verdict is **PASS**:
- Set `phases.uat.status` to `"completed"`
- Set `phases.uat.completedAt` to current timestamp
- Add `docs/uat/sprint-N-uat-report.md` to `phases.uat.artifacts`
- Update `updatedAt`

If verdict is **FAIL**:
- Keep `phases.uat.status` as `"in_progress"`
- Log failure count in state
- Inform user that the UAT gate will not pass until failures are resolved

#### Step 6: Show Next Steps
```
UAT Results — Sprint N
═══════════════════════
✅ Passed: X  |  ❌ Failed: Y  |  ⛔ Blocked: Z

Verdict: [PASS/FAIL]

[If PASS]:
  Next: Run /sdlc next to advance to Security phase
  Or:   Run /uat-setup teardown to stop the environment

[If FAIL]:
  Action: Fix Y failed test cases, then re-run /uat-setup report [sprint]
  Backlog items created: [count] GitHub issues
```

---

### `/uat-setup teardown`

Stop all UAT services and clean up.

#### Step 1: Stop Application Processes
```bash
# Find and stop backend
BACKEND_PIDS=$(pgrep -f "spring-boot:run" 2>/dev/null || pgrep -f "java.*\.jar" 2>/dev/null)
if [ -n "$BACKEND_PIDS" ]; then
  kill $BACKEND_PIDS 2>/dev/null
  echo "Backend stopped"
fi

# Find and stop frontend
FRONTEND_PIDS=$(pgrep -f "ng serve" 2>/dev/null || pgrep -f "node.*angular" 2>/dev/null)
if [ -n "$FRONTEND_PIDS" ]; then
  kill $FRONTEND_PIDS 2>/dev/null
  echo "Frontend stopped"
fi
```

#### Step 2: Stop Docker Containers
```bash
docker compose down -v 2>/dev/null && echo "Docker containers stopped and volumes removed" || echo "No Docker containers to stop"
```

#### Step 3: Clean Up
```bash
# Remove temporary logs
rm -f /tmp/backend-uat.log /tmp/frontend-uat.log 2>/dev/null

echo ""
echo "UAT environment torn down successfully."
echo "All services stopped, containers removed, volumes cleaned."
```

#### Step 4: Update SDLC State
- Remove `phases.uat.environment` from state (PIDs are stale)
- Keep `phases.uat.status` and artifacts intact

---

## Security Reminders
- UAT environment uses LOCAL ports only — never expose to public internet
- Test credentials in seed data are for UAT only — never use in production
- Docker volumes are removed on teardown — no persistent data leakage
- `.env` file should be in `.gitignore` — never commit secrets

---

## Agent Delegation

This skill is primarily executed by the **main orchestrator** (not delegated to a sub-agent) because it requires:
- Interactive user input (AskUserQuestion for test results)
- Long-running process management (backend/frontend PIDs)
- Docker orchestration

However, the **QA Agent** (`.claude/agents/qa-agent.md`) can assist with:
- Generating test case content (Step 2 of test-cases)
- Analyzing failure patterns in the report

The **DevOps Agent** (`.claude/agents/devops-agent.md`) can assist with:
- Docker compose troubleshooting
- Health check debugging
- Port conflict resolution
