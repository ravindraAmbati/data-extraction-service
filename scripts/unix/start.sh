#!/usr/bin/env sh
set -eu

APP_NAME="data-extraction-service"
SCRIPT_DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
PROJECT_DIR="$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)"
APP_JAR="${APP_JAR:-${PROJECT_DIR}/target/${APP_NAME}.jar}"
APP_PROFILE="${APP_PROFILE:-local}"
PID_FILE="${PID_FILE:-${PROJECT_DIR}/${APP_NAME}.pid}"
JAVA_OPTS="${JAVA_OPTS:-}"

if [ -f "$PID_FILE" ] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
  echo "$APP_NAME is already running with PID $(cat "$PID_FILE")"
  exit 0
fi

if [ ! -f "$APP_JAR" ]; then
  echo "JAR not found: $APP_JAR"
  echo "Run: mvn package"
  exit 1
fi

nohup java $JAVA_OPTS -jar "$APP_JAR" --spring.profiles.active="$APP_PROFILE" > "${PROJECT_DIR}/${APP_NAME}.out" 2>&1 &
echo $! > "$PID_FILE"
echo "Started $APP_NAME with PID $(cat "$PID_FILE") using profile $APP_PROFILE"
