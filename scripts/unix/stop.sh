#!/usr/bin/env sh
set -eu

APP_NAME="data-extraction-service"
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
PROJECT_DIR="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
PID_FILE="${PID_FILE:-${PROJECT_DIR}/${APP_NAME}.pid}"

if [ ! -f "$PID_FILE" ]; then
  echo "$APP_NAME is not running"
  exit 0
fi

PID="$(cat "$PID_FILE")"
if kill -0 "$PID" 2>/dev/null; then
  kill "$PID"
  echo "Stopped $APP_NAME with PID $PID"
else
  echo "No running process found for PID $PID"
fi
rm -f "$PID_FILE"
