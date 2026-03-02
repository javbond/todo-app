#!/bin/bash
# scripts/setup-agents.sh — One-time Agent Team Configuration
# Run once after cloning the project to configure agent teams.
#
# Usage: bash scripts/setup-agents.sh

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${BLUE}┌──────────────────────────────────────────────────────────┐${NC}"
echo -e "${BLUE}│          AI-NATIVE SDLC FACTORY — Agent Setup            │${NC}"
echo -e "${BLUE}└──────────────────────────────────────────────────────────┘${NC}"
echo ""

# Step 1: Verify agent definitions exist
echo -e "${YELLOW}Step 1: Checking agent definitions...${NC}"
AGENTS_DIR="$PROJECT_ROOT/.claude/agents"
if [ ! -d "$AGENTS_DIR" ]; then
  echo -e "${RED}✗ .claude/agents/ directory not found${NC}"
  exit 1
fi

AGENT_COUNT=$(ls "$AGENTS_DIR/"*.md 2>/dev/null | wc -l | tr -d ' ')
echo -e "${GREEN}✓ Found ${AGENT_COUNT} agent definitions:${NC}"
for f in "$AGENTS_DIR"/*.md; do
  NAME=$(basename "$f" .md)
  MODEL=$(grep -m1 "^model:" "$f" 2>/dev/null | awk '{print $2}' || echo "unknown")
  DESC=$(grep -m1 "^description:" "$f" 2>/dev/null | sed 's/^description: //' | cut -c1-60 || echo "")
  printf "  %-22s %-8s %s\n" "$NAME" "($MODEL)" "$DESC"
done
echo ""

# Step 2: Configure settings.local.json
echo -e "${YELLOW}Step 2: Configuring agent team settings...${NC}"
SETTINGS="$PROJECT_ROOT/.claude/settings.local.json"

if [ ! -f "$SETTINGS" ]; then
  echo -e "${YELLOW}  Creating $SETTINGS${NC}"
  echo '{}' > "$SETTINGS"
fi

python3 -c "
import json, sys

settings_path = '$SETTINGS'
try:
    with open(settings_path, 'r') as f:
        settings = json.load(f)
except (json.JSONDecodeError, FileNotFoundError):
    settings = {}

# Enable agent teams
settings.setdefault('env', {})
settings['env']['CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS'] = '1'

# Set teammate mode
settings['teammateMode'] = 'tmux'

with open(settings_path, 'w') as f:
    json.dump(settings, f, indent=2)

print('  Settings updated successfully')
" 2>/dev/null || {
  echo -e "${RED}  ✗ Failed to update settings. Ensure python3 is available.${NC}"
  echo -e "${YELLOW}  Manual config: Add to .claude/settings.local.json:${NC}"
  echo '  {"env": {"CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1"}, "teammateMode": "tmux"}'
}

echo -e "${GREEN}✓ Agent teams enabled in settings${NC}"
echo -e "${GREEN}✓ tmux split-pane mode configured${NC}"
echo ""

# Step 3: Verify prerequisites
echo -e "${YELLOW}Step 3: Checking prerequisites...${NC}"

if command -v claude &>/dev/null; then
  CLAUDE_VERSION=$(claude --version 2>/dev/null || echo "unknown")
  echo -e "${GREEN}✓ Claude Code CLI: ${CLAUDE_VERSION}${NC}"
else
  echo -e "${RED}✗ Claude Code CLI not found${NC}"
  echo -e "${YELLOW}  Install: npm install -g @anthropic-ai/claude-code${NC}"
fi

if command -v tmux &>/dev/null; then
  TMUX_VERSION=$(tmux -V 2>/dev/null || echo "unknown")
  echo -e "${GREEN}✓ tmux: ${TMUX_VERSION}${NC}"
else
  echo -e "${YELLOW}⚠ tmux not installed (recommended for split-pane visibility)${NC}"
  echo -e "${YELLOW}  Install: brew install tmux (macOS) or apt install tmux (Linux)${NC}"
fi

if command -v python3 &>/dev/null; then
  echo -e "${GREEN}✓ Python3: $(python3 --version 2>/dev/null)${NC}"
else
  echo -e "${RED}✗ Python3 not found (required for SDLC state management)${NC}"
fi

if command -v gh &>/dev/null; then
  echo -e "${GREEN}✓ GitHub CLI: $(gh --version 2>/dev/null | head -1)${NC}"
else
  echo -e "${YELLOW}⚠ GitHub CLI not installed (required for PR/release operations)${NC}"
  echo -e "${YELLOW}  Install: brew install gh${NC}"
fi

echo ""

# Step 4: Verify project structure
echo -e "${YELLOW}Step 4: Checking project structure...${NC}"

DIRS=(".sdlc" "docs" "scripts" ".claude/skills" ".claude/rules")
for dir in "${DIRS[@]}"; do
  if [ -d "$PROJECT_ROOT/$dir" ]; then
    echo -e "${GREEN}✓ $dir/${NC}"
  else
    echo -e "${RED}✗ $dir/ (missing)${NC}"
  fi
done

echo ""

# Step 5: Summary
echo -e "${BLUE}┌──────────────────────────────────────────────────────────┐${NC}"
echo -e "${BLUE}│                    Setup Complete                         │${NC}"
echo -e "${BLUE}├──────────────────────────────────────────────────────────┤${NC}"
echo -e "${BLUE}│                                                          │${NC}"
echo -e "${BLUE}│  Agents:     ${AGENT_COUNT} definitions loaded                    │${NC}"
echo -e "${BLUE}│  Mode:       tmux split-pane (Agent Teams)               │${NC}"
echo -e "${BLUE}│  Settings:   .claude/settings.local.json                 │${NC}"
echo -e "${BLUE}│                                                          │${NC}"
echo -e "${BLUE}│  To start:   bash scripts/start-sdlc.sh                  │${NC}"
echo -e "${BLUE}│  With phase: bash scripts/start-sdlc.sh design           │${NC}"
echo -e "${BLUE}│                                                          │${NC}"
echo -e "${BLUE}│  Agent Model Assignments:                                │${NC}"
echo -e "${BLUE}│  ┌─────────────────────────────────────────────┐         │${NC}"
echo -e "${BLUE}│  │ THINKERS (Opus):   research, product,       │         │${NC}"
echo -e "${BLUE}│  │                    architect, security,     │         │${NC}"
echo -e "${BLUE}│  │                    review, validator        │         │${NC}"
echo -e "${BLUE}│  │ BUILDERS (Sonnet): backend, frontend, qa   │         │${NC}"
echo -e "${BLUE}│  │ EXECUTORS (Haiku): devops, memory           │         │${NC}"
echo -e "${BLUE}│  └─────────────────────────────────────────────┘         │${NC}"
echo -e "${BLUE}│                                                          │${NC}"
echo -e "${BLUE}└──────────────────────────────────────────────────────────┘${NC}"
