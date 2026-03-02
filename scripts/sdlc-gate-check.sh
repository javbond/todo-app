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

case "$PHASE" in
  ideation)
    check_file_exists "docs/ideation/product-vision.md" || FAILED=1
    if [ $FAILED -eq 0 ]; then
      check_file_contains "docs/ideation/product-vision.md" "Vision" || FAILED=1
      check_file_contains "docs/ideation/product-vision.md" "Problem" || FAILED=1
    fi
    ;;
  requirements)
    check_file_exists "docs/prd/prd.md" || FAILED=1
    if [ $FAILED -eq 0 ]; then
      check_file_contains "docs/prd/prd.md" "Functional Requirements" || FAILED=1
      check_file_contains "docs/prd/prd.md" "Non-Functional Requirements" || FAILED=1
      check_file_contains "docs/prd/prd.md" "Persona" || FAILED=1
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
    # Check that source code directories exist
    if [ ! -d "$PROJECT_ROOT/backend" ] && [ ! -d "$PROJECT_ROOT/frontend" ]; then
      echo "FAIL: No backend/ or frontend/ source code directory found" >&2
      FAILED=1
    fi
    check_dir_not_empty "docs/sprints" || FAILED=1
    ;;
  testing)
    check_dir_not_empty "docs/testing" || FAILED=1
    ;;
  security)
    check_file_exists "docs/security/security-review.md" || FAILED=1
    ;;
  code_review)
    # Check if there's at least one PR
    if command -v gh &> /dev/null; then
      PR_COUNT=$(gh pr list --state all --limit 1 2>/dev/null | wc -l || echo "0")
      if [ "$PR_COUNT" -eq 0 ]; then
        echo "WARN: No pull requests found" >&2
      fi
    fi
    ;;
  release)
    check_dir_not_empty "docs/releases" || FAILED=1
    ;;
  *)
    echo "Unknown phase: $PHASE" >&2
    echo "Valid phases: ideation, requirements, project_setup, design, development, testing, security, code_review, release" >&2
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
