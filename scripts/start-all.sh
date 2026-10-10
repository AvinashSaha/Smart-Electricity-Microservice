#!/usr/bin/env bash
# Starts the whole North East system in the right order (macOS / Linux):
#   service registry -> API gateway -> main program (all cities) -> front end
# Build first:  mvn clean package -DskipTests
# Optional: CITIES=newcastle,durham ./scripts/start-all.sh   (default: all five cities)
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
LOG_DIR="$ROOT/logs"
PID_FILE="$LOG_DIR/sdem.pids"
mkdir -p "$LOG_DIR" "$ROOT/data"
: > "$PID_FILE"

start() { # name, jar, args...
  local name="$1" jar="$2"; shift 2
  if [[ ! -f "$jar" ]]; then
    echo "Missing $jar. Run 'mvn clean package -DskipTests' first." >&2
    exit 1
  fi
  echo "Starting $name ..."
  nohup java "${JAVA_OPTS_ARR[@]}" -jar "$jar" "$@" > "$LOG_DIR/$name.log" 2>&1 &
  echo "$! $name" >> "$PID_FILE"
}

wait_for() { # name, url
  local tries=90
  printf "Waiting for %s " "$1"
  until curl -sf "$2" > /dev/null; do
    tries=$((tries - 1))
    (( tries == 0 )) && { echo " timed out. See logs/$1.log" >&2; exit 1; }
    printf "."; sleep 2
  done
  echo " UP"
}

JAVA_OPTS_ARR=()
start service-registry-app service-registry-app/target/service-registry-app.jar
wait_for service-registry-app http://localhost:8761/actuator/health

start sdem-api-gateway sdem-api-gateway/target/sdem-api-gateway.jar
wait_for sdem-api-gateway http://localhost:8080/actuator/health

JAVA_OPTS_ARR=(-Xmx3g)
start main-program main-program/target/main-program-exec.jar --cities="${CITIES:-newcastle,sunderland,durham,middlesbrough,darlington}"
JAVA_OPTS_ARR=()
printf "Waiting for main-program "
tries=150
until grep -q "Smart City system running" "$LOG_DIR/main-program.log" 2>/dev/null; do
  tries=$((tries - 1))
  (( tries == 0 )) && { echo " timed out. See logs/main-program.log" >&2; exit 1; }
  printf "."; sleep 2
done
echo " UP"

start front-end-app front-end-app/target/front-end-app-exec.jar
wait_for front-end-app http://localhost:8090/actuator/health

cat <<MSG

The North East Smart Electricity system is running.
  Web application : http://localhost:8090   (admin / admin123  or  viewer / viewer123)
  Eureka dashboard: http://localhost:8761
  API gateway     : http://localhost:8080/smartcity-newcastle/api/v1/summary
On a first run the citizen simulation sends 12 months of history; the charts fill in within about a minute.
Logs are in logs/. Stop everything with ./scripts/stop-all.sh
MSG
