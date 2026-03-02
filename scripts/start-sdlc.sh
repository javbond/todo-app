#!/bin/bash
# scripts/start-sdlc.sh — Launch SDLC Factory session with Agent Teams
# Usage: bash scripts/start-sdlc.sh [phase]
#
# This script launches Claude Code with Agent Teams enabled.
# Agents are visible in tmux split panes for real-time collaboration.

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PHASE="${1:-}"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}┌──────────────────────────────────────────────────────────┐${NC}"
echo -e "${BLUE}│          AI-NATIVE SDLC FACTORY — Session Launcher       │${NC}"
echo -e "${BLUE}└──────────────────────────────────────────────────────────┘${NC}"
echo ""

# Enable agent teams
export CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1

# Verify prerequisites
echo -e "${YELLOW}Checking prerequisites...${NC}"

if ! command -v claude &>/dev/null; then
  echo -e "${RED}✗ Claude Code CLI not found. Install: npm install -g @anthropic-ai/claude-code${NC}"
  exit 1
fi
echo -e "${GREEN}✓ Claude Code CLI found${NC}"

if command -v tmux &>/dev/null; then
  echo -e "${GREEN}✓ tmux found — split-pane mode available${NC}"
  TEAMMATE_MODE="tmux"
else
  echo -e "${YELLOW}⚠ tmux not found — using in-process mode (agents cycle in single pane)${NC}"
  echo -e "${YELLOW}  Install tmux for split-pane visibility: brew install tmux${NC}"
  TEAMMATE_MODE="in-process"
fi

# Check agent definitions exist
AGENT_COUNT=$(ls "$PROJECT_ROOT/.claude/agents/"*.md 2>/dev/null | wc -l | tr -d ' ')
if [ "$AGENT_COUNT" -eq 0 ]; then
  echo -e "${RED}✗ No agent definitions found. Run: bash scripts/setup-agents.sh${NC}"
  exit 1
fi
echo -e "${GREEN}✓ ${AGENT_COUNT} agent definitions found${NC}"

# Load project state
echo ""
if [ -f "$PROJECT_ROOT/.sdlc/state.json" ]; then
  CURRENT_PHASE=$(python3 -c "import json; print(json.load(open('$PROJECT_ROOT/.sdlc/state.json'))['currentPhase'])" 2>/dev/null || echo "unknown")
  PROJECT_NAME=$(python3 -c "import json; print(json.load(open('$PROJECT_ROOT/.sdlc/state.json'))['project'])" 2>/dev/null || echo "unknown")
  echo -e "${GREEN}Project: ${PROJECT_NAME}${NC}"
  echo -e "${GREEN}Current Phase: ${CURRENT_PHASE}${NC}"
else
  echo -e "${YELLOW}No project initialized. Run /sdlc init [project-name] after launch.${NC}"
fi

echo ""
echo -e "${BLUE}Available Agents:${NC}"
echo -e "  ${GREEN}THINKERS (Opus):${NC}  research, product, architect, security, review, validator"
echo -e "  ${GREEN}BUILDERS (Sonnet):${NC} backend, frontend, qa"
echo -e "  ${GREEN}EXECUTORS (Haiku):${NC} devops, memory"
echo ""

# Launch Claude Code with agent teams enabled
if [ -n "$PHASE" ]; then
  echo -e "${BLUE}Starting SDLC Factory — Phase: ${PHASE}${NC}"
  echo -e "${YELLOW}Launching Claude Code with teammate-mode: ${TEAMMATE_MODE}...${NC}"
  echo ""
  cd "$PROJECT_ROOT" && claude --teammate-mode "$TEAMMATE_MODE" -p "/sdlc phase $PHASE"
else
  echo -e "${BLUE}Starting SDLC Factory — Interactive Mode${NC}"
  echo -e "${YELLOW}Launching Claude Code with teammate-mode: ${TEAMMATE_MODE}...${NC}"
  echo ""
  echo -e "${GREEN}Quick Commands:${NC}"
  echo "  /sdlc init [project]     — Start new project"
  echo "  /sdlc status             — Show dashboard"
  echo "  /sdlc next               — Advance to next phase"
  echo "  /build-with-agent-team   — Parallel sprint execution"
  echo ""
  cd "$PROJECT_ROOT" && claude --teammate-mode "$TEAMMATE_MODE"
fi
