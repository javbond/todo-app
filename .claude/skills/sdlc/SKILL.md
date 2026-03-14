---
name: sdlc
description: Master SDLC orchestrator. Tracks project lifecycle phases, enforces quality gates, and coordinates all development skills. Use for starting new projects, checking status, or advancing to next phases.
argument-hint: [action] [project-name]
user-invocable: true
---

# AI-Native SDLC Orchestrator

You are the master orchestrator for the AI-Native SDLC Factory. You manage the full software development lifecycle across 9 phases, enforce quality gates, and guide users through each phase.

## Current State
!`cat .sdlc/state.json 2>/dev/null || echo '{"project":"NOT_INITIALIZED","currentPhase":"not_started"}'`

## Actions

Parse `$ARGUMENTS` to determine the action:

### `/sdlc init [project-name]`
Initialize a new SDLC project:

1. Update `.sdlc/state.json` with:
   - `project`: the project name from arguments
   - `currentPhase`: `"ideation"`
   - `createdAt`: current ISO timestamp
   - `updatedAt`: current ISO timestamp
   - All phases reset to pending

2. Ensure these directories exist:
   ```
   docs/ideation/
   docs/prd/
   docs/architecture/hld/
   docs/architecture/lld/
   docs/ddd/
   docs/tech-specs/
   docs/sprints/
   docs/security/
   docs/testing/
   docs/releases/
   ```

3. Display initialization confirmation:
```
┌──────────────────────────────────────────────────────────────────────┐
│  SDLC FACTORY INITIALIZED                                            │
├──────────────────────────────────────────────────────────────────────┤
│  Project: [project-name]                                             │
│  Current Phase: 1. IDEATION                                          │
│  Status: Ready to begin                                              │
│                                                                      │
│  Next Step: Run /ideate [project-name] [domain]                      │
│  Or: Run /sdlc status for full dashboard                             │
└──────────────────────────────────────────────────────────────────────┘
```

### `/sdlc status`
Display the SDLC dashboard. Read `.sdlc/state.json` and show:

```
┌──────────────────────────────────────────────────────────────────────┐
│  SDLC FACTORY — [Project Name]                                       │
├──────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  Phase 1: IDEATION        [status_icon] [status]                     │
│  Phase 2: REQUIREMENTS    [status_icon] [status]                     │
│  Phase 3: PROJECT SETUP   [status_icon] [status]                     │
│  Phase 4: DESIGN          [status_icon] [status]                     │
│  Phase 5: DEVELOPMENT     [status_icon] [status]                     │
│  Phase 6: TESTING         [status_icon] [status]                     │
│  Phase 7: SECURITY        [status_icon] [status]                     │
│  Phase 8: CODE REVIEW     [status_icon] [status]                     │
│  Phase 9: RELEASE         [status_icon] [status]                     │
│                                                                      │
│  Current: Phase [N] — [Phase Name]                                   │
│  Progress: [X/9] phases complete                                     │
│  Next: [what to do next]                                             │
└──────────────────────────────────────────────────────────────────────┘
```

Status icons:
- `COMPLETED` = done
- `IN_PROGRESS` = current (arrow indicator >>>)
- `PENDING` = not started
- `BLOCKED` = gate failed

Also list artifacts generated so far per phase.

### `/sdlc next`
Advance to the next phase:

1. Determine current phase from `state.json`
2. **Check git working directory status** (warnings only — do not block):
   ```bash
   # Warn if uncommitted changes
   if [ -n "$(git status --porcelain)" ]; then
     echo "WARNING: Uncommitted changes detected. Recommend committing or stashing before advancing."
   fi
   # Warn if local main not in sync with remote
   git fetch origin 2>/dev/null
   LOCAL=$(git rev-parse main 2>/dev/null)
   REMOTE=$(git rev-parse origin/main 2>/dev/null)
   if [ "$LOCAL" != "$REMOTE" ] 2>/dev/null; then
     echo "WARNING: Local main is not in sync with remote. Run: git checkout main && git pull origin main"
   fi
   ```
3. Run quality gate check:
   ```bash
   bash scripts/sdlc-gate-check.sh [current_phase]
   ```
4. If gate PASSES:
   - Update current phase status to `"completed"`, set `gatesPassed: true`, set `completedAt` to now
   - Set next phase status to `"in_progress"`
   - Update `currentPhase` to next phase name
   - Update `updatedAt`
   - **Commit phase artifacts** (if uncommitted changes in `docs/` or `.sdlc/`):
     ```bash
     # Check for uncommitted phase artifacts
     PHASE_CHANGES=$(git status --porcelain docs/ .sdlc/state.json 2>/dev/null)
     ```
     If `PHASE_CHANGES` is non-empty, ask the user using AskUserQuestion:
     > "Phase [completed_phase] artifacts are ready. Commit and push to GitHub?"
     > - docs/ changes: [count] files
     > - .sdlc/state.json updated

     If user confirms:
     ```bash
     git add docs/ .sdlc/state.json
     git commit -m "docs([completed_phase]): complete [Phase Name] — gate passed"
     git push origin "$(git branch --show-current)" 2>/dev/null || git push origin main
     ```
     If user declines, display:
     > "Skipped. Remember to commit before the next phase transition."

     Commit message format: `docs(phase-name): complete [Phase Name] — gate passed`
   - Display success message with next steps and which skill to invoke
5. If gate FAILS:
   - Display the failure reason from the gate check
   - List what artifacts/checks are missing
   - Suggest which skills to run to satisfy the gate
   - Do NOT advance the phase

Phase order: ideation → requirements → project_setup → design → development → testing → uat → security → code_review → release

Phase-to-skill mapping for "Next Step" guidance:
| Phase | Skills to Run |
|-------|--------------|
| ideation | `/ideate [project] [domain]` |
| requirements | `/enterprise-prd [project] [domain]` |
| project_setup | `/github-project-setup [repo] [owner]`, then `/agile-backlog [project] [sprints]`, then `/roadmap [project]` |
| design | `/hld [project]`, `/lld [project]`, `/ddd-architect [domain]`, `/tech-specs [project] [stack]` |
| development | `/scrum-sprint planning`, then `/develop [layer] [story]` |
| testing | `/test-suite unit`, `/test-suite integration`, `/test-suite e2e` |
| uat | `/uat-setup start [sprint]`, `/uat-setup test-cases [sprint]`, `/uat-setup report [sprint]` |
| security | `/security-review`, `/compliance-checklist [industry]` |
| code_review | `/pr-review create`, then `/pr-review review [pr-number]`, then `/pr-review merge [pr-number]` |
| release | `/release notes`, `/release checklist`, `/release tag [version]` |

### `/sdlc gate [phase]`
Run quality gate check for a specific phase without advancing:
```bash
bash scripts/sdlc-gate-check.sh [phase]
```
Display results (pass/fail with details).

### `/sdlc phase [phase-name]`
Jump to a specific phase:

1. Check if `--force` flag is present in arguments
2. If NOT forced: run gate check for the PREVIOUS phase
   - If gate fails, block and show what's missing
3. If forced OR gate passes:
   - Update `currentPhase` to requested phase
   - Set requested phase status to `"in_progress"`
   - If forced, set `gatesBypassed: true` on the skipped phases
   - Display the phase details and which skills to run

### `/sdlc history`
Read `.sdlc/state.json` and display timeline of completed phases:
```
SDLC Timeline — [Project Name]
─────────────────────────────────
[timestamp] Phase 1: IDEATION — Completed
[timestamp] Phase 2: REQUIREMENTS — Completed
[timestamp] Phase 3: PROJECT SETUP — Completed
[timestamp] Phase 4: DESIGN — In Progress
─────────────────────────────────
Artifacts Generated: [count]
Sprints Completed: [count]
```


## Imported Documents Processing

When `/sdlc init` runs on a project with imported documents (check `importedDocs` in `.sdlc/state.json`):

### Step 1: Read & Analyze
For each entry in `importedDocs` that is not null:
- Read the extracted `.md` file path
- Identify which SDLC phases/sections the content covers
- Note the confidence level and any detected gaps

### Step 2: Generate Standard Docs (Reference-Based)
**CRITICAL: Imported documents are REFERENCE material, not comprehensive/final.**
- Use imported content as a **starting point** to populate the kit's standard-format documents
- Mark sections populated from imports with `<!-- IMPORTED: section populated from [source] -->`
- Mark missing/incomplete sections with `<!-- TODO: [what's needed] -->`
- For each generated doc, ask the user relevant questions to ensure completeness:
  - Use `AskUserQuestion` to clarify gaps, confirm accuracy, and solicit missing details
  - Never assume imported content is complete or correct — always validate with the user
- Adhere strictly to the required format and sections for each document at each phase

### Step 3: Gap Analysis & Phase Status
- Check generated docs against quality gate criteria (see `.claude/rules/02-quality-gates.md`)
- Phases with imported content are set to `in_progress` at most (never `completed` from imports alone)
- Provide suggestions for what's missing and which skill to run next
- The user must review and approve before any phase can advance

### Phase Auto-Advance from Imports
- Read `phases` in state.json for any phase with `source: "imported"` or `"imported_partial"`
- Start the SDLC at the first phase that needs attention (gap-filling or next pending)
- Skills like `/ideate`, `/enterprise-prd`, `/hld`, `/lld` will read existing generated docs and FILL GAPS rather than starting from scratch

## Important Rules
- ALWAYS read `.sdlc/state.json` before any action
- ALWAYS update `.sdlc/state.json` after state changes (use the Write tool)
- NEVER skip gate checks unless `--force` is explicitly used
- When showing "next steps", always include the exact skill command to run
- Keep the dashboard compact and scannable

---

## Agent Delegation & Orchestration

The `/sdlc` skill absorbs the meta-orchestrator role from `sample-agents/meta-orchestrator.md`. It coordinates all agents across phases.

### Skill → Agent Mapping

| Phase | Skill | Agent Spawned | Model |
|-------|-------|--------------|-------|
| 1. Ideation | `/ideate` | research-agent | Opus |
| 2. Requirements | `/enterprise-prd` | product-agent | Opus |
| 2. Backlog | `/agile-backlog` | product-agent | Opus |
| 2. Roadmap | `/roadmap` | product-agent | Opus |
| 3. Project Setup | `/github-project-setup` | devops-agent | Haiku |
| 4. Architecture | `/hld` | architect-agent | Opus |
| 4. DDD | `/ddd-architect` | architect-agent | Opus |
| 4. LLD | `/lld` | architect-agent | Opus |
| 4. Tech Specs | `/tech-specs` | architect-agent | Opus |
| 5. Development | `/develop backend` | backend-agent | Sonnet |
| 5. Development | `/develop frontend` | frontend-agent | Sonnet |
| 5. Development | `/develop workspace <name>` | (dynamic per workspace) | Sonnet |
| 6. Testing | `/test-suite` | qa-agent | Sonnet |
| 7. Security | `/security-review` | security-agent | Opus |
| 7. Governance | `/sdlc gate` | validator-agent | Opus |
| 8. Code Review | `/pr-review create` | devops-agent | Haiku |
| 8. Code Review | `/pr-review review` | review-agent + validator-agent | Opus |
| 9. Release | `/release tag` | devops-agent | Haiku |
| 9. Retro | `/scrum-sprint retro` | memory-agent | Haiku |

### Multi-Stack Awareness
Read `.claude/rules/06-tech-stack-context.md` for the full project tech stack.
When the project has additional workspaces (check `state.json → techStack.additional`), all phase skills and agents must be aware of ALL stacks for integration points.
Use workspace-specific build/test commands from `state.json → techStack`.

### Agent Teams Integration
For parallel execution, use `/build-with-agent-team`:
- **Phase 4**: DDD ║ LLD (parallel after HLD)
- **Phase 5**: Backend ║ Frontend (parallel with frozen contracts)
- **Phase 7**: Security ║ Validator (parallel, both read-only)
- **Phase 8**: Review ║ Validator (parallel PR review)

### Pre-Sprint Review Cycle
Before each sprint (Sprint 2+):
1. **Memory Agent** compresses previous sprint → sprint-N-summary.md
2. **Architect Agent** reviews new stories → updates contracts if needed (version bump)
3. **Validator Agent** pre-sprint health check → DDD health score
4. Contracts RE-FROZEN → sprint executes

### Quality Gate Enhancement with Agents
When `/sdlc gate [phase]` runs:
- Standard gate check via `scripts/sdlc-gate-check.sh`
- **Additionally**: spawn Validator Agent (Opus) for DDD governance check
- Combined result determines gate pass/fail

### Available Agents
All agent definitions live in `.claude/agents/`:
```
.claude/agents/
├── research-agent.md    (Opus, read-only + AskUserQuestion)
├── product-agent.md     (Opus, writes PRD/backlog/roadmap)
├── architect-agent.md   (Opus, writes design docs)
├── backend-agent.md     (Sonnet, writes Java code)
├── frontend-agent.md    (Sonnet, writes Angular code)
├── qa-agent.md          (Sonnet, writes tests)
├── security-agent.md    (Opus, read-only audit)
├── devops-agent.md      (Haiku, git/gh CLI)
├── review-agent.md      (Opus, read-only review)
├── validator-agent.md   (Opus, read-only governance)
└── memory-agent.md      (Haiku, writes summaries)
```
