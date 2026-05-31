#!/usr/bin/env bash
set -euo pipefail

# Wrapper to call Copilot CLI (if available) to get assistant reply, then log the prompt+summary
# Usage: .github/copilot-auto-log.sh "<prompt>" "<short-summary (optional)>"

PROMPT="${1:-}"
SUMMARY="${2:-}"

if [ -z "$PROMPT" ]; then
  echo "Usage: $0 \"<prompt>\" \"<short-summary (optional)>\""
  exit 2
fi

# Helper: shell-escape the prompt for safe insertion into evaluated candidate commands
escape_prompt(){ printf '%q' "$1"; }
ESC_PROMPT=$(escape_prompt "$PROMPT")

# Candidate command templates (use %s for escaped prompt)
candidates=(
  "copilot ask --prompt %s --non-interactive"
  "copilot ask --prompt %s --json"
  "gh copilot chat --message %s --json"
  "gh copilot chat -m %s --json"
)

ASSIST_REPLY=""

# Try each candidate command if its binary is present
for tmpl in "${candidates[@]}"; do
  # detect which binary the template references
  if echo "$tmpl" | grep -q "^copilot" && command -v copilot >/dev/null 2>&1; then
    cmd=$(printf "$tmpl" "$ESC_PROMPT")
  elif echo "$tmpl" | grep -q "^gh" && command -v gh >/dev/null 2>&1; then
    cmd=$(printf "$tmpl" "$ESC_PROMPT")
  else
    continue
  fi

  # Try to run the command; capture stdout
  set +e
  out=$(eval "$cmd" 2>/dev/null || true)
  rc=$?
  set -e
  if [ $rc -eq 0 ] && [ -n "$(echo "$out" | tr -d '\n' )" ]; then
    ASSIST_REPLY="$out"
    break
  fi
done

# Fallback: open editor for manual paste
if [ -z "$ASSIST_REPLY" ]; then
  echo "No Copilot CLI detected or automated commands failed; falling back to manual entry."
  tmpfile=$(mktemp /tmp/copilot_reply.XXXXXX)
  cat > "$tmpfile" <<-EOF
Prompt:
$PROMPT

# Paste assistant reply below this line (remove these comment lines), save and exit to continue.

EOF
  ${EDITOR:-vi} "$tmpfile"
  # read everything after the marker line
  ASSIST_REPLY=$(sed -n '/# Paste assistant reply below this line/{n;:a;p;n;ba}' "$tmpfile" 2>/dev/null || true)
  rm -f "$tmpfile"
fi

# If SUMMARY not provided, derive it from assistant reply (first non-empty line, truncated)
if [ -z "$SUMMARY" ]; then
  SUMMARY=$(printf '%s' "$ASSIST_REPLY" | awk 'NF{print; exit}' | tr '\n' ' ' | sed -E 's/[[:space:]]+/ /g' | sed -E 's/^ //; s/ $//')
  SUMMARY=${SUMMARY:0:200}
  if [ -z "$SUMMARY" ]; then
    SUMMARY="(assistant provided no summary)"
  fi
fi

# Call the logger (relative to this script)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
"$SCRIPT_DIR/copilot-log.sh" "$PROMPT" "$SUMMARY"

echo "Logged: $SUMMARY"
