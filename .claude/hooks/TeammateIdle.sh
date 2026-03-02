#!/bin/bash
# .claude/hooks/TeammateIdle.sh
# Hook triggered when an agent teammate becomes idle.
#
# This hook notifies the lead that an agent has no tasks and suggests
# what it could work on next based on the current SDLC phase.
#
# Usage: This hook is automatically invoked by Claude Code Agent Teams
# when a teammate finishes all assigned tasks and has nothing to do.

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
AGENT_NAME="${1:-unknown}"

echo "[SDLC] Agent idle: $AGENT_NAME"

# Determine current phase
if [ -f "$PROJECT_ROOT/.sdlc/state.json" ]; then
  CURRENT_PHASE=$(python3 -c "import json; print(json.load(open('$PROJECT_ROOT/.sdlc/state.json'))['currentPhase'])" 2>/dev/null || echo "unknown")
else
  CURRENT_PHASE="unknown"
fi

# Suggest next action based on agent type and current phase
case "$AGENT_NAME" in
  *backend*)
    echo "[SDLC] Suggestions for idle backend agent:"
    case "$CURRENT_PHASE" in
      development)
        echo "  - Pick next unassigned backend story from sprint plan"
        echo "  - Write additional unit tests for existing code"
        echo "  - Refactor within current bounded context"
        ;;
      testing)
        echo "  - Help QA agent with integration test setup"
        echo "  - Fix failing tests"
        ;;
      *)
        echo "  - Review DDD aggregate designs"
        echo "  - Prepare for next sprint"
        ;;
    esac
    ;;

  *frontend*)
    echo "[SDLC] Suggestions for idle frontend agent:"
    case "$CURRENT_PHASE" in
      development)
        echo "  - Pick next unassigned frontend story from sprint plan"
        echo "  - Add component tests for existing features"
        echo "  - Improve UI accessibility"
        ;;
      testing)
        echo "  - Help QA agent with E2E test setup"
        echo "  - Fix failing component tests"
        ;;
      *)
        echo "  - Review Angular feature module structure"
        echo "  - Prepare for next sprint"
        ;;
    esac
    ;;

  *qa*)
    echo "[SDLC] Suggestions for idle QA agent:"
    echo "  - Review coverage gaps"
    echo "  - Add edge case tests"
    echo "  - Write E2E tests for critical user flows"
    ;;

  *security*)
    echo "[SDLC] Security agent idle — all scans complete"
    echo "  - Review dependency vulnerabilities"
    echo "  - Deep dive into authentication flows"
    ;;

  *validator*)
    echo "[SDLC] Validator agent idle — governance checks complete"
    echo "  - Run drift detection"
    echo "  - Calculate updated coupling score"
    ;;

  *review*)
    echo "[SDLC] Review agent idle — awaiting PR for review"
    ;;

  *)
    echo "[SDLC] Agent $AGENT_NAME is idle"
    echo "  - Check task list (Ctrl+T) for available tasks"
    ;;
esac

echo "[SDLC] Lead: assign a new task or dismiss this agent"
