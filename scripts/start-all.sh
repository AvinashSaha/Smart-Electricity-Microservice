#!/usr/bin/env bash
# Starts every SDEM service in the correct order (macOS / Linux).
# Build first with:  mvn clean package -DskipTests
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
LOG_DIR="$ROOT/logs"
PID_FILE="$LOG_DIR/sdem.pids"
mkdir -p "$LOG_DIR"
: > "$PID_FILE"

start() {
  local module="$1" port="$2"
  local jar="$ROOT/$module/target/$module.jar"
  if [[ ! -f "$jar" ]]; then
    echo "Missing $jar. Run 'mvn clean package -DskipTests' first." >&2
    exit 1
  fi
  echo "Starting $module on port $port ..."
  nohup java -jar "$jar" > "$LOG_DIR/$module.log" 2>&1 &
  echo "$! $module" >> "$PID_FILE"
}

wait_for() {
  local module="$1" port="$2" tries=90
  printf "Waiting for %s " "$module"
  until curl -sf "http://localhost:$port/actuator/health" > /dev/null; do
    tries=$((tries - 1))
    if (( tries == 0 )); then
      echo " timed out. See logs/$module.log" >&2
      exit 1
    fi
    printf "."
    sleep 2
  done
  echo " UP"
}

start service-registry-app 8761
wait_for service-registry-app 8761

start electricity-provider-app 8081
start citizen-app 8082
start smart-city-app 8083
wait_for electricity-provider-app 8081
wait_for citizen-app 8082
wait_for smart-city-app 8083

start sdem-api-gateway 8080
wait_for sdem-api-gateway 8080

start front-end-app 8090
wait_for front-end-app 8090

cat <<MSG

All services are running.
  Web application : http://localhost:8090
  Eureka dashboard: http://localhost:8761
  API gateway     : http://localhost:8080/api/providers
Give the gateway about 30 seconds to discover every service.
Logs are in logs/. Stop everything with ./scripts/stop-all.sh
MSG
