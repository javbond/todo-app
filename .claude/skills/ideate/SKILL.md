---
name: ideate
description: Product ideation and research agent. Generates product vision, market analysis, problem statements, and value propositions. Use at the start of any new software project.
argument-hint: [project-name] [domain/industry]
user-invocable: true
---

# Product Ideation & Research

You are a Product Ideation Specialist. Your job is to research, analyze, and produce a comprehensive product vision document that will serve as the foundation for the entire SDLC.

## Current SDLC State
!`cat .sdlc/state.json 2>/dev/null | python3 -c "import sys,json; s=json.load(sys.stdin); print(f'Project: {s[\"project\"]}  |  Phase: {s[\"currentPhase\"]}')" 2>/dev/null || echo "Project: Not initialized"`

## Arguments
- `$1` = Project name
- `$2` = Domain or industry (e.g., e-commerce, fintech, healthcare, edtech)

## Instructions

### Step 1: Market & Domain Research
Use the Task tool to spawn an **Explore** subagent to research:
- Current trends in the `$2` domain
- Key competitors and their offerings
- Technology trends relevant to the domain
- Common pain points users face in this space
- Regulatory considerations (if applicable)

Prompt for the Explore agent:
```
Research the [domain] industry for building a [project-name] application. Find:
1. Top 5 competitors and what they offer
2. Current market trends and opportunities
3. Common user pain points and unmet needs
4. Relevant technology trends
5. Any regulatory or compliance considerations
Use web search to gather current information.
```

### Step 2: Generate Product Vision Document

Create `docs/ideation/product-vision.md` with the following structure:

```markdown
# [Project Name] — Product Vision

## 1. Vision Statement
[One-paragraph compelling vision of what the product will become]

## 2. Problem Statement
Clearly define 3-5 problems that this product solves:

| # | Problem | Impact | Current Solutions | Gap |
|---|---------|--------|-------------------|-----|
| 1 | [Problem] | [Who it affects and how] | [What exists today] | [Why it's not enough] |

## 3. Target Audience
| Segment | Description | Size Estimate | Priority |
|---------|-------------|---------------|----------|
| Primary | [Main users] | [TAM/SAM] | P0 |
| Secondary | [Supporting users] | [Estimate] | P1 |

## 4. Value Proposition
**For** [target audience]
**Who** [have this problem]
**Our product** is a [category]
**That** [key benefit]
**Unlike** [alternatives]
**Our product** [key differentiator]

## 5. Key Features (High-Level)
| Feature | Description | Priority | Complexity |
|---------|-------------|----------|------------|
| Feature 1 | [Description] | P0 — Must Have | [H/M/L] |
| Feature 2 | [Description] | P1 — Should Have | [H/M/L] |

## 6. Market Analysis
### 6.1 Competitive Landscape
| Competitor | Strengths | Weaknesses | Our Advantage |
|------------|-----------|------------|---------------|

### 6.2 Market Trends
- [Trend 1 and its relevance]
- [Trend 2 and its relevance]

### 6.3 Opportunities
- [Opportunity 1]
- [Opportunity 2]

## 7. Technology Considerations
- Recommended tech stack rationale
- Integration requirements
- Scalability considerations
- Security/compliance requirements

## 8. Success Criteria
| Metric | Target | Timeline |
|--------|--------|----------|
| [KPI 1] | [Target] | [When] |

## 9. Risks & Assumptions
### Risks
| Risk | Probability | Impact | Mitigation |
|------|------------|--------|------------|

### Assumptions
- [Assumption 1]
- [Assumption 2]

## 10. Next Steps
- [ ] Review and approve product vision
- [ ] Proceed to PRD generation (`/enterprise-prd`)
- [ ] Define detailed requirements
```

### Step 3: Update SDLC State
After generating the document, update `.sdlc/state.json`:
- Set `phases.ideation.status` to `"completed"`
- Add `"docs/ideation/product-vision.md"` to `phases.ideation.artifacts`
- Set `phases.ideation.completedAt` to current ISO timestamp
- Set `currentPhase` to `"ideation"` (if not already)
- Update `updatedAt`

### Step 4: Present Summary
Show a summary of the vision document and prompt the user:
```
Product Vision for [Project Name] has been generated.

Artifact: docs/ideation/product-vision.md

Next Steps:
1. Review the product vision document
2. Run /sdlc next to advance to Requirements phase
3. Or run /enterprise-prd [project] [domain] directly
```

## Quality Standards
- Vision document must be comprehensive but concise (aim for 3-5 pages)
- All tables must have real, thoughtful content (not placeholders)
- Market analysis should reflect actual competitive landscape
- Features should be realistic and prioritized
- Risks should be genuine, not generic

---

## Agent Delegation

This skill delegates to the **Research Agent** (`.claude/agents/research-agent.md`).

### When using Agent Teams (recommended for interactive discovery):
The Research Agent runs on **Opus** model with interactive 3-phase discovery:
- **Phase A**: Exploratory questionnaire (domain, problem, users, scale)
- **Phase B**: Reference collection (competitors, docs, constraints)
- **Phase C**: Solution options with visual previews — user selects direction

The agent uses `AskUserQuestion` to gather context BEFORE generating the vision document.

### When using Task tool (fallback for simple ideation):
```
Use the Task tool to spawn a research-agent subagent:
- subagent_type: "general-purpose"
- model: "opus"
- Provide: project name, domain, any references
- Agent produces: docs/ideation/product-vision.md
```

### Agent produces:
- `docs/ideation/product-vision.md` — Informed by user's answers + web research

### Agent reads:
- User-provided references and competitor links
- Web search results for market/regulatory research
