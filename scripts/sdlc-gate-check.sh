#!/bin/bash
# SDLC Quality Gate Checker
# Usage: bash scripts/sdlc-gate-check.sh [phase]
# Exit codes: 0 = pass, 2 = fail

set -e

PHASE="${1:-}"
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
STATE_FILE="$PROJECT_ROOT/.sdlc/state.json"

if [ -z "$PHASE" ]; then
  echo "Usage: bash scripts/sdlc-gate-check.sh [phase]" >&2
  echo "Phases: ideation, requirements, project_setup, design, development, testing, security, code_review, release" >&2
  exit 1
fi

if [ ! -f "$STATE_FILE" ]; then
  echo "FAIL: .sdlc/state.json not found. Run '/sdlc init' first." >&2
  exit 2
fi

check_file_exists() {
  if [ ! -f "$PROJECT_ROOT/$1" ]; then
    echo "FAIL: Required artifact missing: $1" >&2
    return 1
  fi
  return 0
}

check_dir_not_empty() {
  if [ ! -d "$PROJECT_ROOT/$1" ] || [ -z "$(ls -A "$PROJECT_ROOT/$1" 2>/dev/null)" ]; then
    echo "FAIL: Required directory empty or missing: $1" >&2
    return 1
  fi
  return 0
}

check_file_contains() {
  local file="$PROJECT_ROOT/$1"
  local pattern="$2"
  if [ ! -f "$file" ] || ! grep -q "$pattern" "$file" 2>/dev/null; then
    echo "FAIL: $1 missing required content: $pattern" >&2
    return 1
  fi
  return 0
}

FAILED=0

# Check if imported docs satisfy gate requirements
check_imported_docs() {
  local phase="$1"
  if command -v python3 &>/dev/null && [ -f "$STATE_FILE" ]; then
    python3 -c "
import json
with open('$STATE_FILE') as f:
    s = json.load(f)
phase_data = s.get('phases', {}).get('$phase', {})
if phase_data.get('source') in ['imported', 'manual']:
    exit(0)
exit(1)
" 2>/dev/null
    return $?
  fi
  return 1
}

case "$PHASE" in
  ideation)
    if check_imported_docs "ideation"; then
      echo "INFO: Ideation gate satisfied via imported/manual docs" >&2
    else
      check_file_exists "docs/ideation/product-vision.md" || FAILED=1
      if [ $FAILED -eq 0 ]; then
        check_file_contains "docs/ideation/product-vision.md" "Vision" || FAILED=1
        check_file_contains "docs/ideation/product-vision.md" "Problem" || FAILED=1
      fi
    fi
    ;;
  requirements)
    if check_imported_docs "requirements"; then
      echo "INFO: Requirements gate satisfied via imported/manual docs" >&2
    else
      check_file_exists "docs/prd/prd.md" || FAILED=1
      if [ $FAILED -eq 0 ]; then
        check_file_contains "docs/prd/prd.md" "Functional Requirements" || FAILED=1
        check_file_contains "docs/prd/prd.md" "Non-Functional Requirements" || FAILED=1
        check_file_contains "docs/prd/prd.md" "Persona" || FAILED=1
      fi
    fi
    ;;
  project_setup)
    check_file_exists "docs/prd/roadmap.md" || FAILED=1
    # Check if GitHub repo exists (non-blocking if gh not configured)
    if command -v gh &> /dev/null; then
      REPO=$(cat "$STATE_FILE" | python3 -c "import sys,json; print(json.load(sys.stdin).get('github',{}).get('repo',''))" 2>/dev/null || echo "")
      if [ -n "$REPO" ]; then
        gh repo view "$REPO" > /dev/null 2>&1 || echo "WARN: GitHub repo '$REPO' not accessible" >&2
      fi
    fi
    ;;
  design)
    check_dir_not_empty "docs/architecture/hld" || FAILED=1
    check_dir_not_empty "docs/architecture/lld" || FAILED=1
    ;;
  development)
    # Dynamic: read workspace directories from state.json
    SOURCE_DIRS=""
    if command -v python3 &>/dev/null && [ -f "$STATE_FILE" ]; then
      SOURCE_DIRS=$(python3 -c "
import json
with open('$STATE_FILE') as f:
    s = json.load(f)
dirs = []
ts = s.get('techStack', {})
p = ts.get('primary', {})
if p.get('frontend'):
    dirs.append(p['frontend'].get('directory', 'frontend'))
if p.get('backend'):
    dirs.append(p['backend'].get('directory', 'backend'))
for w in ts.get('additional', []):
    if w.get('directory'):
        dirs.append(w['directory'])
print(' '.join(dirs))
" 2>/dev/null)
    fi
    # Fallback to default directories
    if [ -z "$SOURCE_DIRS" ]; then
      SOURCE_DIRS="backend frontend"
    fi

    SOURCE_FOUND=false
    for dir in $SOURCE_DIRS; do
      if [ -d "$PROJECT_ROOT/$dir" ] && [ "$(ls -A "$PROJECT_ROOT/$dir" 2>/dev/null)" ]; then
        SOURCE_FOUND=true
        break
      fi
    done

    if [ "$SOURCE_FOUND" = false ]; then
      echo "FAIL: No source code directory found (checked: $SOURCE_DIRS)" >&2
      FAILED=1
    fi
    check_dir_not_empty "docs/sprints" || FAILED=1
    ;;
  testing)
    check_dir_not_empty "docs/testing" || FAILED=1
    ;;
  uat)
    if check_imported_docs "uat"; then
      echo "INFO: UAT gate satisfied via imported/manual docs" >&2
    else
      check_dir_not_empty "docs/uat" || FAILED=1
      if [ $FAILED -eq 0 ]; then
        UAT_REPORT=$(find "$PROJECT_ROOT/docs/uat" -name "*uat-report*" -type f 2>/dev/null | head -1)
        if [ -z "$UAT_REPORT" ]; then
          echo "FAIL: No UAT report found in docs/uat/" >&2
          FAILED=1
        fi
      fi
    fi
    ;;
  security)
    check_file_exists "docs/security/security-review.md" || FAILED=1
    ;;
  code_review)
    # Check if remote origin exists before running PR checks
    if ! git remote get-url origin &>/dev/null; then
      echo "WARN: No remote origin configured — skipping PR merge checks" >&2
    elif command -v gh &> /dev/null; then
      # Check for merged PRs targeting main (GitHub Flow)
      MERGED_COUNT=$(gh pr list --state merged --base main --limit 50 --json number 2>/dev/null | python3 -c "import sys,json; print(len(json.load(sys.stdin)))" 2>/dev/null || echo "0")
      if [ "$MERGED_COUNT" -eq 0 ]; then
        echo "FAIL: No merged pull requests found targeting main" >&2
        FAILED=1
      fi

      # Check that the most recent merged PR had at least 1 approval (warning only)
      LATEST_APPROVED=$(gh pr list --state merged --base main --limit 1 --json number,reviewDecision 2>/dev/null | python3 -c "
import sys, json
prs = json.load(sys.stdin)
if prs and prs[0].get('reviewDecision') == 'APPROVED':
    print('yes')
else:
    print('no')
" 2>/dev/null || echo "unknown")
      if [ "$LATEST_APPROVED" = "no" ]; then
        echo "WARN: Most recent merged PR was not approved via review" >&2
      fi

      # Check local main sync with remote (warning only)
      git fetch origin 2>/dev/null
      LOCAL_MAIN=$(git rev-parse main 2>/dev/null || echo "none")
      REMOTE_MAIN=$(git rev-parse origin/main 2>/dev/null || echo "none")
      if [ "$LOCAL_MAIN" != "$REMOTE_MAIN" ] && [ "$LOCAL_MAIN" != "none" ]; then
        echo "WARN: Local main ($LOCAL_MAIN) is not in sync with origin/main ($REMOTE_MAIN)" >&2
        echo "WARN: Run 'git checkout main && git pull origin main' to sync" >&2
      fi
    else
      echo "WARN: gh CLI not available — cannot verify PR merge status" >&2
    fi
    ;;
  release)
    check_dir_not_empty "docs/releases" || FAILED=1
    ;;
  *)
    echo "Unknown phase: $PHASE" >&2
    echo "Valid phases: ideation, requirements, project_setup, design, development, testing, uat, security, code_review, release" >&2
    exit 1
    ;;
esac

if [ $FAILED -eq 1 ]; then
  echo "GATE CHECK FAILED for phase: $PHASE" >&2
  exit 2
else
  echo "GATE CHECK PASSED for phase: $PHASE"
  exit 0
fi
