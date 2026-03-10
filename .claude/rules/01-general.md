> **Multi-Stack Note**: File naming and code organization conventions below are the defaults for Angular + Spring Boot + DDD.
> For the FULL project tech stack, see `.claude/rules/06-tech-stack-context.md`.
> Additional workspaces may follow different conventions per their reference docs in `docs/tech-refs/`.

# General Standards

## File Naming
- Java: PascalCase for classes (`UserService.java`), camelCase for methods
- TypeScript/Angular: kebab-case for files (`user-list.component.ts`)
- SQL: snake_case for tables and columns
- Documentation: kebab-case markdown files (`product-vision.md`)

## Code Organization
- Follow DDD layered architecture for all application code
- One class per file (Java), one component per file (Angular)
- Group by bounded context, not by technical layer
- Keep methods under 30 lines; extract when larger

## Git Workflow (GitHub Flow)
- Branch from `main`, merge back via PR
- Branch naming: `feature/US-XXX-short-desc`, `bugfix/BUG-XXX-short-desc`
- Commit messages: `type(scope): description` (conventional commits)
  - `feat(user)`: new feature
  - `fix(auth)`: bug fix
  - `docs(api)`: documentation
  - `test(user)`: adding tests
  - `refactor(order)`: code refactoring
  - `chore(deps)`: maintenance
- PR title matches the primary user story: `[US-XXX] Short description`
- Squash merge to main

### Branch Sync Requirements
- **Pre-development**: Always `git fetch origin && git pull origin main` before creating a feature branch
- **Post-merge**: After every PR merge, sync local main: `git checkout main && git pull origin main`
- **Post-release**: After tagging, push tag to remote. No release branches needed.
- **Clean working directory**: Verify `git status --porcelain` is empty before phase transitions

### Post-Merge Workflow
After a PR is squash-merged to main:
1. Sync local main: `git checkout main && git pull origin main`
2. Delete local feature branch: `git branch -d feature/US-XXX-short-desc`
3. Verify sync: confirm local and remote main SHAs match

### Branch Cleanup
- Feature branches: deleted (local + remote) after PR merge
- Never leave stale branches — clean up within same session as merge

## Documentation Requirements Per Phase
| Phase | Required Artifacts |
|-------|-------------------|
| Ideation | `docs/ideation/product-vision.md` |
| Requirements | `docs/prd/prd.md` |
| Project Setup | GitHub repo + `docs/prd/roadmap.md` + `docs/prd/backlog.md` |
| Design | `docs/architecture/hld/`, `docs/architecture/lld/`, `docs/ddd/`, `docs/tech-specs/` |
| Development | Source code + `docs/sprints/sprint-N-plan.md` |
| Testing | Test reports in `docs/testing/` |
| Security | `docs/security/security-review.md`, `docs/security/compliance-report.md` |
| Code Review | PR created and linked to issues |
| Release | `docs/releases/vX.Y.Z-release-notes.md` |

## Error Handling
- Never swallow exceptions silently
- Use domain-specific exception hierarchy
- Log errors with context (who, what, when, correlation ID)
- Return structured error responses (code, message, details)
