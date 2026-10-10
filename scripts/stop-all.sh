#!/usr/bin/env bash
# Stops the services started by start-all.sh
set -uo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
PID_FILE="$ROOT/logs/sdem.pids"

if [[ ! -f "$PID_FILE" ]]; then
  echo "No running services recorded (logs/sdem.pids not found)."
  exit 0
fi

while read -r pid module; do
  if kill "$pid" 2> /dev/null; then
    echo "Stopped $module (pid $pid)"
  fi
done < "$PID_FILE"
rm -f "$PID_FILE"
