#!/bin/sh
# Simple logger for Copilot sessions.
# Usage: .github/copilot-log.sh "prompt text" "short summary"
LOG_FILE="$(dirname "$0")/copilot_session_log.md"

timestamp=$(date +'%d/%m/%Y %H:%M')
prompt="$1"
summary="$2"

# Escape double quotes in prompt for safe display
escaped_prompt=$(printf '%s' "$prompt" | sed 's/"/\\"/g')

# Default summary if none provided
if [ -z "$summary" ]; then
  summary="(no summary provided)"
fi

printf '* * %s\tprompt **"%s"**\t%s\n' "$timestamp" "$escaped_prompt" "$summary" >> "$LOG_FILE"
