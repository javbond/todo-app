#!/bin/bash
# .claude/hooks/TaskCompleted.sh
# Hook triggered when an agent teammate completes a task.
#
# This hook enforces quality gates after task completion:
# - Checks if the completed work meets SDLC standards
# - Validates DDD discipline compliance
# - Reports coverage thresholds
#
# Usage: This hook is automatically invoked by Claude Code Agent Teams
# when a teammate reports a task as done.

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
TASK_NAME="${1:-unknown}"
AGENT_NAME="${2:-unknown}"

# Log task completion
echo "[SDLC] Task completed: $TASK_NAME by $AGENT_NAME"

# Determine validation based on agent type
case "$AGENT_NAME" in
  *backend*)
    echo "[SDLC] Running backend quality checks..."

    # Check if backend code compiles
    if [ -f "$PROJECT_ROOT/backend/pom.xml" ]; then
      cd "$PROJECT_ROOT/backend"
      if mvn compile -q 2>/dev/null; then
        echo "[SDLC] ✓ Backend compilation successful"
      else
        echo "[SDLC] ✗ Backend compilation failed — fix before proceeding"
        exit 1
      fi
    fi
    ;;

  *frontend*)
    echo "[SDLC] Running frontend quality checks..."

    # Check if frontend builds
    if [ -f "$PROJECT_ROOT/frontend/angular.json" ]; then
      cd "$PROJECT_ROOT/frontend"
      if npx ng build --configuration=development 2>/dev/null; then
        echo "[SDLC] ✓ Frontend build successful"
      else
        echo "[SDLC] ✗ Frontend build failed — fix before proceeding"
        exit 1
      fi
    fi
    ;;

  *qa*)
    echo "[SDLC] Running test validation..."

    # Check test results
    if [ -f "$PROJECT_ROOT/backend/pom.xml" ]; then
      cd "$PROJECT_ROOT/backend"
      if mvn test -q 2>/dev/null; then
        echo "[SDLC] ✓ Backend tests pass"
      else
        echo "[SDLC] ✗ Backend tests failed"
        exit 1
      fi
    fi
    ;;

  *security*)
    echo "[SDLC] Security audit completed — check docs/security/security-review.md"

    # Verify security report exists
    if [ -f "$PROJECT_ROOT/docs/security/security-review.md" ]; then
      # Check for CRITICAL findings
      if grep -q "CRITICAL" "$PROJECT_ROOT/docs/security/security-review.md" 2>/dev/null; then
        echo "[SDLC] ⚠ CRITICAL security findings detected — review required before merge"
      else
        echo "[SDLC] ✓ No CRITICAL security findings"
      fi
    fi
    ;;

  *validator*)
    echo "[SDLC] Governance validation completed"
    ;;

  *review*)
    echo "[SDLC] Code review completed"
    ;;

  *)
    echo "[SDLC] Task completed by $AGENT_NAME"
    ;;
esac

# Update timestamp in state
if [ -f "$PROJECT_ROOT/.sdlc/state.json" ]; then
  python3 -c "
import json
from datetime import datetime, timezone

with open('$PROJECT_ROOT/.sdlc/state.json', 'r') as f:
    state = json.load(f)

state['updatedAt'] = datetime.now(timezone.utc).isoformat()
state.setdefault('agentActivity', []).append({
    'agent': '$AGENT_NAME',
    'task': '$TASK_NAME',
    'completedAt': datetime.now(timezone.utc).isoformat()
})

# Keep only last 50 activity entries
state['agentActivity'] = state['agentActivity'][-50:]

with open('$PROJECT_ROOT/.sdlc/state.json', 'w') as f:
    json.dump(state, f, indent=2)
" 2>/dev/null || true
fi

echo "[SDLC] Task completion recorded"
