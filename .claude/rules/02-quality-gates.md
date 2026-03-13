# Quality Gates

Quality gates enforce that each SDLC phase is complete before the next can begin. The `/sdlc next` command checks these gates before advancing.

## Gate Definitions

### Gate 1: Ideation → Requirements
**Check:** `docs/ideation/product-vision.md` exists and contains:
- Product vision statement
- Problem statement (at least 3 problems)
- Target audience definition
- Value proposition
**Status field:** `phases.ideation.gatesPassed`

### Gate 2: Requirements → Project Setup
**Check:** `docs/prd/prd.md` exists and contains:
- Executive summary
- At least 3 user personas
- Functional requirements (FR-XXX format)
- Non-functional requirements (NFR-XXX format)
- Success metrics
- Roadmap outline
**Status field:** `phases.requirements.gatesPassed`

### Gate 3: Project Setup → Design
**Check:** All of the following:
- GitHub repo exists (check via `gh repo view`)
- `docs/prd/roadmap.md` exists with milestones
- `docs/prd/backlog.md` exists OR GitHub issues with `epic` label exist
- GitHub project board exists
**Status field:** `phases.project_setup.gatesPassed`

### Gate 4: Design → Development
**Check:** All of the following:
- `docs/architecture/hld/` contains at least one HLD document
- `docs/architecture/lld/` contains at least one LLD document
- `docs/ddd/` contains DDD artifacts OR `/ddd-architect` output exists
- `docs/tech-specs/` contains technical specifications
**Status field:** `phases.design.gatesPassed`

### Gate 5: Development → Testing
**Check:** All of the following:
- Source code exists in `backend/` and/or `frontend/` directories
- Build succeeds (Maven/Gradle for backend, ng build for frontend)
- No compilation errors
- At least one sprint plan exists in `docs/sprints/`
**Status field:** `phases.development.gatesPassed`

### Gate 6: Testing → UAT
**Check:** All of the following:
- Unit test coverage >= 80% (backend), >= 70% (frontend)
- All unit tests pass
- Integration tests exist and pass
- Test report saved to `docs/testing/`
**Status field:** `phases.testing.gatesPassed`

### Gate 7: UAT → Security
**Check:** All of the following:
- `docs/uat/` contains at least one UAT report
- UAT report verdict is PASS (all Critical/High test cases passed)
- No CRITICAL UAT failures without resolution
- Failed items logged as backlog issues or fixed in sprint
**Status field:** `phases.uat.gatesPassed`

### Gate 8: Security → Code Review
**Check:** All of the following:
- `docs/security/security-review.md` exists
- No CRITICAL severity findings open
- No HIGH severity findings without mitigation plan
- Compliance checklist completed if applicable
**Status field:** `phases.security.gatesPassed`

### Gate 9: Code Review → Release
**Check:** All of the following:
- PR created and linked to user stories
- At least 1 approval on PR
- All CI checks passing on PR
- No unresolved review comments
**Status field:** `phases.code_review.gatesPassed`

### Gate 10: Release Complete
**Check:** All of the following:
- `docs/releases/` contains release notes
- Git tag created for version
- Deployment checklist completed
- GitHub release created
**Status field:** `phases.release.gatesPassed`

## Gate Check Script
Run `bash scripts/sdlc-gate-check.sh [phase]` to validate a specific gate.
Exit code 0 = pass, exit code 2 = fail (reason printed to stderr).

## Override Policy
Gates can be bypassed with `/sdlc phase [name] --force`, but this will be logged in state.json with a `gatesBypassed: true` flag and reason. Use only when absolutely necessary.
