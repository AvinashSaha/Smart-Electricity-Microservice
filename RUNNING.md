# How to Run: Smart Electricity Microservice

This is the run specification for the North East Smart Electricity system. It explains how to build the
solution, start every part in the right order, sign in to the web application, check that everything works,
run the tests and fix common problems.

There are three ways to run it:

- **Option A:** command line (recommended, one script starts everything)
- **Option B:** IntelliJ IDEA or Eclipse
- **Option C:** Docker Compose (no Java or Maven needed)

---

## 1. What runs

| # | Part | Port(s) | Started by |
|---|---|---|---|
| 1 | Service registry (Eureka) | 8761 | `service-registry-app` |
| 2 | API gateway | 8080 | `sdem-api-gateway` |
| 3 | Main program: for each of the 5 cities, 1 Smart City + 3 Electricity Provider + 1 Citizen microservice | 8110 to 8154 | `main-program` |
| 4 | Web application (front end) | 8090 | `front-end-app` |

Port blocks used by the main program:

| City | Smart City | Provider A | Provider B | Provider C | Citizens |
|---|---|---|---|---|---|
| Newcastle | 8110 | 8111 | 8112 | 8113 | 8114 |
| Sunderland | 8120 | 8121 | 8122 | 8123 | 8124 |
| Durham | 8130 | 8131 | 8132 | 8133 | 8134 |
| Middlesbrough | 8140 | 8141 | 8142 | 8143 | 8144 |
| Darlington | 8150 | 8151 | 8152 | 8153 | 8154 |

All of these ports must be free on `localhost`.

---

## 2. Prerequisites

| Tool | Version | Needed for | Check with |
|---|---|---|---|
| Java JDK | 17 or newer | Options A and B | `java -version` |
| Apache Maven | 3.8 or newer | Option A (IntelliJ has Maven built in) | `mvn -v` |
| Git | any | Cloning | `git --version` |
| Docker Desktop | 24 or newer | Option C only | `docker compose version` |
| Memory | about 4 GB free | all five cities | |

macOS (Homebrew): `brew install openjdk@17 maven git`

Windows: install [Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17),
[Maven](https://maven.apache.org/download.cgi) (add its `bin` folder to `PATH`) and
[Git for Windows](https://git-scm.com/download/win).

---

## 3. Get the code and build it

```bash
git clone https://github.com/AvinashSaha/Smart-Electricity-Microservice.git
cd Smart-Electricity-Microservice
mvn clean package
```

This compiles every project, runs the unit and integration tests, and creates these runnable jars:

```
service-registry-app/target/service-registry-app.jar
sdem-api-gateway/target/sdem-api-gateway.jar
main-program/target/main-program-exec.jar
front-end-app/target/front-end-app-exec.jar
```

(The microservice projects also produce `*-exec.jar` files so a single instance can be run on its own; see section 9.)

Skip the tests for a faster build: `mvn clean package -DskipTests`. The first build downloads dependencies and needs
an internet connection.

**Always run the commands from the repository root**, because the database files are read from and written to
`./data`.

---

## 4. Start order (important)

1. **Service registry** first, and wait until http://localhost:8761 opens.
2. **API gateway**, and wait until http://localhost:8080/actuator/health shows `UP`.
3. **Main program** (all the microservices). Wait for the log line `Smart City system running`.
4. **Front end**.

The citizen and smart city services talk to the providers through the gateway, and the gateway finds them through
the registry, so the registry and gateway must be up first. After the main program starts, allow **about 30 to 60
seconds** for every instance to register and for the first data to be collected.

**First run only:** the citizen simulation sends 12 months of history for every citizen and smart meter (about
30,000 readings across the region). This takes a minute or two; the charts fill in as each city finishes. If the
committed sample databases in `data/` are present, this step is skipped and data appears straight away.

---

## 5. Option A: command line

### A1. One script (macOS / Linux)

```bash
mvn clean package -DskipTests
./scripts/start-all.sh
```

It starts the four parts in order, waits for each, writes logs to `logs/` and prints the addresses. To run fewer
cities (less memory): `CITIES=newcastle,durham ./scripts/start-all.sh`

Stop everything with:

```bash
./scripts/stop-all.sh
```

### A2. By hand (any operating system)

Open four terminals in the repository root and run, in order:

```bash
# Terminal 1: service registry
java -jar service-registry-app/target/service-registry-app.jar

# Terminal 2: API gateway
java -jar sdem-api-gateway/target/sdem-api-gateway.jar

# Terminal 3: main program (all five cities)
java -Xmx3g -jar main-program/target/main-program-exec.jar

# Terminal 4: web application
java -jar front-end-app/target/front-end-app-exec.jar
```

Main program options:

| Option | Default | Example |
|---|---|---|
| `--cities=` | all five | `--cities=newcastle,sunderland` |
| `--data-dir=` | `./data` | `--data-dir=/tmp/sdem-data` |
| `--gateway-url=` | `http://localhost:8080` | |
| `--eureka-url=` | `http://localhost:8761/eureka` | |
| any `--key=value` | | passed to every microservice, e.g. `--sdem.simulation.tick-interval=PT10S` |

Stop each part with `Ctrl + C`.

---

## 6. Option B: IntelliJ IDEA (or Eclipse)

1. **File > Open**, choose the repository folder (the one with the parent `pom.xml`) and open it as a Maven project.
2. Set **File > Project Structure > Project > SDK** to Java 17.
3. Set each run configuration's **working directory** to the repository root (so `./data` is found).
4. Run these main classes in this order:

| Order | Main class | Project |
|---|---|---|
| 1 | `com.sdem.registry.ServiceRegistryApplication` | service-registry-app |
| 2 | `com.sdem.gateway.ApiGatewayApplication` | sdem-api-gateway |
| 3 | `com.sdem.main.MainProgram` (add VM option `-Xmx3g`) | main-program |
| 4 | `com.sdem.web.FrontEndApplication` | front-end-app |

Eclipse: File > Import > Existing Maven Projects, then Run As > Java Application on the same classes.

---

## 7. Option C: Docker Compose

```bash
docker compose up --build
```

The first build takes several minutes. Compose starts the registry, then the gateway, then the main program and the
front end. The `data/` folder is mounted into the main program container, so data is kept between runs.

```bash
docker compose ps
docker compose logs -f main-program
docker compose down
```

---

## 8. Use and check the system

### 8.1 Sign in to the web application

Open **http://localhost:8090** and sign in:

| User name | Password | Role | Can do |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | everything, including creating and deleting users |
| `viewer` | `viewer123` | USER | view data, change own password |

Users are held in memory, so any users you create disappear when the front end restarts.

### 8.2 Demo walk-through

1. **Region overview:** total consumption, monthly average per citizen, cities reporting (should be 5 / 5), the
   *Monthly average per citizen, all cities* chart, and a card per city.
2. Switch the period between **3, 6 and 12 months**; switch a chart between **Line** and **Bar**; click legend
   entries to hide or show a line.
3. Open a **city** (for example Newcastle): the data table of the city's providers by month and the chart of each
   provider's monthly average.
4. Open a **provider** (for example Provider B): the same provider across all five cities.
5. **Change password** (any user).
6. As `admin`, open **Manage users**: create a user, sign out, sign in as that user, then delete them as admin.
7. **Resilience:** stop the main program; the pages show cities as *Unavailable* instead of failing. Start it again
   and data returns.

### 8.3 Other useful pages

| What | URL |
|---|---|
| Eureka dashboard (should list 25 instances plus the gateway) | http://localhost:8761 |
| Gateway routes | http://localhost:8080/actuator/gateway/routes |
| Swagger UI, Newcastle provider A | http://localhost:8111/swagger-ui.html |
| Swagger UI, Newcastle smart city | http://localhost:8110/swagger-ui.html |
| Swagger UI, Newcastle citizens | http://localhost:8114/swagger-ui.html |
| Front end version info | http://localhost:8090/actuator/info |

### 8.4 Calling the APIs through the gateway

```bash
# Smart city: stored monthly aggregates and totals
curl "http://localhost:8080/smartcity-newcastle/api/v1/aggregates?months=6"
curl  http://localhost:8080/smartcity-newcastle/api/v1/summary
curl -X POST http://localhost:8080/smartcity-newcastle/api/v1/aggregates/collect

# Provider: monthly totals, latest readings, summary
curl "http://localhost:8080/provider-durham-b/api/v1/consumption/monthly?months=3"
curl "http://localhost:8080/provider-durham-b/api/v1/readings?limit=5"
curl  http://localhost:8080/provider-durham-b/api/v1/summary

# Citizens: list, simulation status, manual entry (use a value higher than lastReadingKwh)
curl "http://localhost:8080/citizen-sunderland/api/v1/citizens?provider=a"
curl  http://localhost:8080/citizen-sunderland/api/v1/simulation/status
curl  http://localhost:8080/citizen-sunderland/api/v1/citizens/SUNDERLAND-A-M001
curl -X POST http://localhost:8080/citizen-sunderland/api/v1/citizens/SUNDERLAND-A-M001/readings \
     -H "Content-Type: application/json" -d '{"readingKwh": 99999.5}'
```

On Windows PowerShell use `curl.exe`, or import the Postman collection `postman/SDEM.postman_collection.json`.

### 8.5 Databases

Each instance has its own file in `data/` (see [data/README.md](data/README.md)). To look inside one while it is
running, open its H2 console, for example http://localhost:8111/h2-console, with JDBC URL
`jdbc:h2:file:./data/provider-newcastle-a`, user `sa` and an empty password.

To start from empty, stop everything and delete all `data/*.mv.db` files together.

---

## 9. Running a single microservice on its own

Useful for development. Each service needs the registry (or `--eureka.client.enabled=false`):

```bash
java -jar electricity-provider-app/target/electricity-provider-app-exec.jar \
     --sdem.provider.city=newcastle --sdem.provider.code=a --server.port=8111

java -jar smart-city-app/target/smart-city-app-exec.jar --sdem.city.city=newcastle --server.port=8110

java -jar citizen-app/target/citizen-app-exec.jar --sdem.simulation.city=newcastle --server.port=8114
```

Settings live in each project's `provider.yml`, `smartcity.yml`, `citizen.yml` and `frontend.yml`. Useful ones:

| Setting | Default | Meaning |
|---|---|---|
| `sdem.simulation.manual-per-provider` | 100 | manual citizens per provider |
| `sdem.simulation.smart-per-provider` | 50 | smart meters per provider |
| `sdem.simulation.smart-increment-min` / `-max` | 0.002 / 0.020 kWh | range of each smart meter increase |
| `sdem.simulation.tick-interval` | PT30S | how often meters send readings |
| `sdem.simulation.backfill-months` | 12 | months of history sent on first run |
| `sdem.city.collect-interval` | PT30S | how often a smart city collects from its providers |
| `sdem.frontend.users` | admin, viewer | initial users of the web application |

---

## 10. Tests

```bash
mvn test                              # all tests
mvn -pl unit-tests -am test           # unit tests of the microservices
mvn -pl integration-tests -am test    # integration tests of the front end and microservices
```

Neither test project needs the registry, gateway or any running service: the integration tests start their own
providers, smart city, citizen simulation and front end on free ports. Reports are written to
`<project>/target/surefire-reports/`.

---

## 11. Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| Pages say a city is *Unavailable* just after starting | instances still registering | wait 30 to 60 seconds and refresh |
| City page says *No data has been collected* | first run history still being sent | wait a minute; see `curl http://localhost:8080/citizen-newcastle/api/v1/simulation/status` (`pendingBackfill` falls to 0) |
| `503` from the gateway | service not registered yet, or not running | check http://localhost:8761 |
| `Port ... already in use` | another program on that port | stop it (`lsof -i :8080` on macOS, `netstat -ano \| findstr :8080` on Windows) |
| `OutOfMemoryError` in the main program | not enough heap for 25 instances | start with `-Xmx3g`, or run fewer cities with `--cities=` |
| `Database may be already in use` | a second copy is using the same files | stop the other copy; only one main program per data folder |
| Readings rejected after deleting some database files | citizen and provider databases out of step | stop everything and delete **all** `data/*.mv.db` files together |
| `invalid target release: 17` | Maven using an older JDK | set `JAVA_HOME` to JDK 17 |
| Forgot a changed password | users are in memory | restart the front end; the initial users come back |

---

## 12. Stopping

| Started with | Stop with |
|---|---|
| `start-all.sh` | `./scripts/stop-all.sh` |
| Terminals | `Ctrl + C` in each (main program first) |
| IntelliJ | Stop buttons |
| Docker Compose | `docker compose down` |
