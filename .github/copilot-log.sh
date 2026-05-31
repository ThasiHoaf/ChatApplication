#!/usr/bin/env bash
set -euo pipefail

# Simple safe logger for Copilot sessions
# Usage: .github/copilot-log.sh "<prompt-text>" "<short-summary>"
# Appends a single-line entry to .github/copilot_session_log.md; never truncates existing content.

LOG_FILE="$(dirname "$0")/copilot_session_log.md"
PROMPT="${1:-}"
SUMMARY="${2:-}"
TS="$(date '+%d/%m/%Y %H:%M')"

# Normalize to single line and strip excessive whitespace
sanitize(){ echo "$1" | tr '\t' ' ' | tr '\n' ' ' | sed -E 's/[[:space:]]+/ /g' | sed -E 's/^ //; s/ $//' ; }
P_ONELINE="$(sanitize "$PROMPT")"
S_ONELINE="$(sanitize "$SUMMARY")"

mkdir -p "$(dirname "$LOG_FILE")"
# Ensure the file exists; do not overwrite if it already does
if [ ! -f "$LOG_FILE" ]; then
  : > "$LOG_FILE"
fi

# If file empty, add header (append-only)
if [ ! -s "$LOG_FILE" ]; then
  printf "%s\n\n" "# Copilot session log" >> "$LOG_FILE"
fi

# Protect against empty entries
if [ -z "$P_ONELINE" ] && [ -z "$S_ONELINE" ]; then
  echo "No prompt or summary provided; nothing to append." >&2
  exit 1
fi

# Append entry atomically if flock available
if command -v flock >/dev/null 2>&1; then
  (
    flock -x 200
    printf "* * %s\tprompt **\"%s\"**\t%s\n" "$TS" "$P_ONELINE" "$S_ONELINE" >&200
  ) 200>>"$LOG_FILE"
else
  printf "* * %s\tprompt **\"%s\"**\t%s\n" "$TS" "$P_ONELINE" "$S_ONELINE" >> "$LOG_FILE"
fi
