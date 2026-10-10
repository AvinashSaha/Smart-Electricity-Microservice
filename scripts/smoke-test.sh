#!/usr/bin/env bash
# End to end check of the whole system: starts the registry, gateway, main program
# (all five cities) and front end from the built jars, waits for the simulation to
# produce data, checks it through the gateway and the web UI, then stops everything.
# Used by the sample-data GitHub workflow. Build first: mvn -B package -DskipTests
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
mkdir -p logs data
PIDS=()
cleanup() {
  echo "Stopping services..."
  for (( i=${#PIDS[@]}-1; i>=0; i-- )); do kill "${PIDS[$i]}" 2>/dev/null || true; done
  for pid in "${PIDS[@]}"; do wait "$pid" 2>/dev/null || true; done
}
trap cleanup EXIT

wait_http() { # url, seconds
  local end=$((SECONDS + $2))
  until curl -sf "$1" > /dev/null; do
    (( SECONDS > end )) && { echo "Timed out waiting for $1"; tail -50 logs/*.log; exit 1; }
    sleep 3
  done
}

java -jar service-registry-app/target/service-registry-app.jar > logs/registry.log 2>&1 & PIDS+=($!)
wait_http http://localhost:8761/actuator/health 120
java -jar sdem-api-gateway/target/sdem-api-gateway.jar > logs/gateway.log 2>&1 & PIDS+=($!)
wait_http http://localhost:8080/actuator/health 120
java -Xmx3g -jar main-program/target/main-program-exec.jar --data-dir=./data \
  --sdem.simulation.tick-interval=PT10S --sdem.city.collect-interval=PT15S > logs/main-program.log 2>&1 & PIDS+=($!)
java -jar front-end-app/target/front-end-app-exec.jar > logs/front-end.log 2>&1 & PIDS+=($!)
wait_http http://localhost:8090/actuator/health 180

echo "Waiting for every city to have aggregated data through the gateway..."
for city in newcastle sunderland durham middlesbrough darlington; do
  end=$((SECONDS + 600))
  while true; do
    total=$(curl -sf "http://localhost:8080/smartcity-$city/api/v1/summary" | python3 -c "import sys,json;print(json.load(sys.stdin)['totalKwh'])" 2>/dev/null || echo 0)
    if python3 -c "import sys; sys.exit(0 if float('$total') > 0 else 1)"; then
      echo "  $city: $total kWh"; break
    fi
    (( SECONDS > end )) && { echo "No data for $city"; tail -80 logs/main-program.log; exit 1; }
    sleep 10
  done
done

echo "Letting live readings run for a minute..."
sleep 60

echo "Provider through gateway:"; curl -sf http://localhost:8080/provider-newcastle-a/api/v1/summary; echo
echo "Citizen simulation:"; curl -sf http://localhost:8080/citizen-newcastle/api/v1/simulation/status; echo

echo "Front end sign in and city page:"
JAR=$(mktemp)
TOKEN=$(curl -sf -c "$JAR" -b "$JAR" http://localhost:8090/login | sed -n 's/.*name="_csrf" value="\([^"]*\)".*/\1/p' | head -1)
curl -sf -c "$JAR" -b "$JAR" -o /dev/null -d "username=admin&password=admin123&_csrf=$TOKEN" http://localhost:8090/login
PAGE=$(curl -sf -c "$JAR" -b "$JAR" http://localhost:8090/cities/newcastle)
echo "$PAGE" | grep -q "Newcastle by provider" || { echo "City page missing table"; exit 1; }
DASH=$(curl -sf -c "$JAR" -b "$JAR" http://localhost:8090/)
echo "$DASH" | grep -q "5 / 5" || { echo "Not all cities reporting on dashboard"; exit 1; }
echo "  dashboard shows 5 / 5 cities reporting"
echo "Smoke test passed."
