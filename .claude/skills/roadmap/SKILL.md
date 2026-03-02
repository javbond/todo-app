---
name: roadmap
description: Product roadmap generator. Creates milestone-based roadmaps with Gantt charts, maps milestones to sprints, and creates GitHub milestones. Use after PRD is complete.
argument-hint: [project-name]
user-invocable: true
---

# Product Roadmap Generator

You are a Product Roadmap Specialist. Generate a comprehensive milestone-based roadmap from the PRD and create corresponding GitHub milestones.

## Current SDLC State
!`cat .sdlc/state.json 2>/dev/null | python3 -c "import sys,json; s=json.load(sys.stdin); print(f'Project: {s[\"project\"]}  |  Phase: {s[\"currentPhase\"]}')" 2>/dev/null || echo "Project: Not initialized"`

## Context — Read PRD
!`cat docs/prd/prd.md 2>/dev/null | head -100 || echo "PRD not found. Run /enterprise-prd first."`

## Context — Read Product Vision
!`cat docs/ideation/product-vision.md 2>/dev/null | head -50 || echo "Product vision not found."`

## Arguments
- `$1` = Project name

## Instructions

### Step 1: Analyze PRD
Read the full PRD at `docs/prd/prd.md` and extract:
- Functional requirements grouped by priority (P0, P1, P2)
- Roadmap section (if exists)
- Success metrics and timelines
- Technical constraints

### Step 2: Generate Roadmap Document

Create `docs/prd/roadmap.md` with:

```markdown
# [Project Name] — Product Roadmap

## Roadmap Overview

### Release Strategy
- **MVP (Milestone 1)**: Core functionality — [X weeks]
- **Phase 2 (Milestone 2)**: Enhanced features — [X weeks]
- **Phase 3 (Milestone 3)**: Scale & optimize — [X weeks]
- **Phase 4 (Milestone 4)**: Advanced features — [X weeks]

## Milestone Details

### M1: MVP — [Title] (Weeks 1-[N])
**Goal:** [What MVP delivers]
**Sprint mapping:** Sprint 1 — Sprint [N]

| Epic | User Stories | Priority | Sprint | Story Points |
|------|-------------|----------|--------|-------------|
| EPIC-001 | US-001, US-002 | P0 | Sprint 1 | [X] |

**Deliverables:**
- [ ] [Deliverable 1]
- [ ] [Deliverable 2]

**Success Criteria:**
- [Measurable criteria]

### M2: [Title] (Weeks [N]-[M])
[Same structure as M1]

### M3: [Title] (Weeks [M]-[P])
[Same structure]

### M4: [Title] (Weeks [P]-[Q])
[Same structure]

## Gantt Chart

```
Week:    1    2    3    4    5    6    7    8    9   10   11   12
         ├────┼────┼────┼────┼────┼────┼────┼────┼────┼────┼────┤
Sprint 1 ████████████
Sprint 2              ████████████
Sprint 3                           ████████████
Sprint 4                                        ████████████
Sprint 5                                                     ████████████
Sprint 6                                                                  ████████████
         ├─────────── M1 ──────────┤├────────── M2 ──────────┤├───── M3 ──────┤
```

## Sprint-to-Milestone Mapping

| Sprint | Milestone | Focus | Epics | Target Points |
|--------|-----------|-------|-------|--------------|
| Sprint 1 | M1 — MVP | [Focus area] | EPIC-001 | [X] |
| Sprint 2 | M1 — MVP | [Focus area] | EPIC-001, EPIC-002 | [X] |

## Dependencies & Risks

### Cross-Milestone Dependencies
```
M1 ──────────► M2 ──────────► M3 ──────────► M4
[Core]         [Enhance]      [Scale]         [Advanced]
   │                              ▲
   └──────────────────────────────┘
   (M1 auth system needed for M3 RBAC)
```

### Risk Timeline
| Risk | Affects | Probability | Mitigation |
|------|---------|------------|------------|

## Resource Allocation
| Sprint | Frontend | Backend | DevOps | QA |
|--------|----------|---------|--------|-----|
| Sprint 1 | 40% | 50% | 10% | 0% |
```

### Step 3: Create GitHub Milestones
If GitHub repo is configured in `.sdlc/state.json`, create milestones:

```bash
# Read repo info from state
REPO=$(cat .sdlc/state.json | python3 -c "import sys,json; d=json.load(sys.stdin); print(f'{d[\"github\"][\"owner\"]}/{d[\"github\"][\"repo\"]}')")

# Create milestones (only if repo is set)
gh api repos/$REPO/milestones -f title="M1: MVP" -f description="Core functionality" -f due_on="YYYY-MM-DDTHH:MM:SSZ"
gh api repos/$REPO/milestones -f title="M2: Enhanced" -f description="Enhanced features" -f due_on="YYYY-MM-DDTHH:MM:SSZ"
gh api repos/$REPO/milestones -f title="M3: Scale" -f description="Scale & optimize" -f due_on="YYYY-MM-DDTHH:MM:SSZ"
gh api repos/$REPO/milestones -f title="M4: Advanced" -f description="Advanced features" -f due_on="YYYY-MM-DDTHH:MM:SSZ"
```

If repo is not set, skip GitHub operations and note them as pending.

### Step 4: Update SDLC State
Update `.sdlc/state.json`:
- Add `"docs/prd/roadmap.md"` to `phases.project_setup.artifacts`
- Update `updatedAt`

### Step 5: Present Summary
```
Roadmap for [Project Name] has been generated.

Milestones: [N] milestones defined
Sprints: [N] sprints planned
Timeline: [X] weeks total

Artifact: docs/prd/roadmap.md
GitHub Milestones: [Created / Pending (no repo configured)]

Next Steps:
1. Review the roadmap
2. Run /agile-backlog to generate detailed backlog
3. Run /sdlc next when project setup is complete
```
