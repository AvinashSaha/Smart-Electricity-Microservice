# How to Run: Smart Electricity Microservice

This document is the run specification for the SDEM system. It covers everything needed to build, start,
check and stop the six services, using any one of three methods:

- **Option A:** command line with Maven (recommended for first run)
- **Option B:** IntelliJ IDEA (or Eclipse)
- **Option C:** Docker Compose (no Java or Maven needed on the machine)

---

## 1. Prerequisites

| Tool | Version | Needed for | Check with |
|---|---|---|---|
| Java JDK | 17 or newer | Options A and B | `java -version` |
| Apache Maven | 3.8 or newer | Option A (IntelliJ has Maven built in) | `mvn -v` |
| Git | any | Cloning the repository | `git --version` |
| Docker Desktop | 24 or newer | Option C only | `docker compose version` |
| IntelliJ IDEA | 2023 or newer | Option B only | |

**Installing on macOS (Homebrew):**

```bash
brew install openjdk@17 maven git
```

**Installing on Windows:** install [Temurin JDK 17](https://adoptium.net/temurin/releases/?version=17),
[Maven](https://maven.apache.org/download.cgi) (add its `bin` folder to `PATH`) and
[Git for Windows](https://git-scm.com/download/win).

**Free ports.** The system uses these ports on `localhost`. Make sure nothing else is using them:

| Port | Service |
|---|---|
| 8761 | service-registry-app (Eureka) |
| 8080 | sdem-api-gateway |
| 8081 | electricity-provider-app |
| 8082 | citizen-app |
| 8083 | smart-city-app |
| 8090 | front-end-app |

---

## 2. Get the code

```bash
git clone https://github.com/AvinashSaha/Smart-Electricity-Microservice.git
cd Smart-Electricity-Microservice
```

---

## 3. Build

From the repository root (the folder that contains the parent `pom.xml`):

```bash
mvn clean package
```

This compiles all six modules, runs all tests and creates one runnable jar per module:

```
service-registry-app/target/service-registry-app.jar
sdem-api-gateway/target/sdem-api-gateway.jar
electricity-provider-app/target/electricity-provider-app.jar
citizen-app/target/citizen-app.jar
smart-city-app/target/smart-city-app.jar
front-end-app/target/front-end-app.jar
```

To build faster without running the tests: `mvn clean package -DskipTests`

The first build downloads dependencies from Maven Central, so it needs an internet connection and can take a few
minutes. Later builds are much quicker.

---

## 4. Start order (important)

Services must start in this order, because the registry has to be up before anything can register with it:

| Step | Service | Wait until |
|---|---|---|
| 1 | `service-registry-app` | `http://localhost:8761` shows the Eureka dashboard |
| 2 | `electricity-provider-app` | log shows `Started ElectricityProviderApplication` |
| 3 | `citizen-app` | log shows `Started CitizenApplication` |
| 4 | `smart-city-app` | log shows `Started SmartCityApplication` |
| 5 | `sdem-api-gateway` | log shows `Started ApiGatewayApplication` |
| 6 | `front-end-app` | log shows `Started FrontEndApplication` |

Steps 2 to 4 can be started at the same time. After everything is up, **allow about 30 seconds** for the
services to appear in Eureka and for the gateway to pick them up. Calls made before then may return
`503 Service Unavailable`; simply try again.

---

## 5. Option A: run from the command line

### A1. Using the helper script (macOS / Linux)

```bash
mvn clean package -DskipTests      # build once
./scripts/start-all.sh             # starts all six services in the right order
```

The script waits for each service to report healthy, writes logs to `logs/<service>.log` and saves process IDs
so you can stop everything with:

```bash
./scripts/stop-all.sh
```

### A2. Starting each service by hand

Open **six terminal windows**, all in the repository root, and run one command in each, in this order:

```bash
# Terminal 1
java -jar service-registry-app/target/service-registry-app.jar

# Terminal 2
java -jar electricity-provider-app/target/electricity-provider-app.jar

# Terminal 3
java -jar citizen-app/target/citizen-app.jar

# Terminal 4
java -jar smart-city-app/target/smart-city-app.jar

# Terminal 5
java -jar sdem-api-gateway/target/sdem-api-gateway.jar

# Terminal 6
java -jar front-end-app/target/front-end-app.jar
```

Alternatively, run straight from source without building jars first:

```bash
mvn -pl service-registry-app spring-boot:run
mvn -pl electricity-provider-app spring-boot:run
# ...and so on for each module
```

Stop a service with `Ctrl + C` in its terminal.

---

## 6. Option B: run in IntelliJ IDEA

1. **File > Open** and choose the repository folder (the one with the parent `pom.xml`). Open it as a Maven project.
2. Wait for IntelliJ to import the project and download dependencies (progress bar bottom right).
3. Set the SDK: **File > Project Structure > Project > SDK = 17**.
4. Open the main class of each service and click the green ▶ next to `main`, in the start order above:

| Service | Main class |
|---|---|
| service-registry-app | `com.sdem.registry.ServiceRegistryApplication` |
| electricity-provider-app | `com.sdem.provider.ElectricityProviderApplication` |
| citizen-app | `com.sdem.citizen.CitizenApplication` |
| smart-city-app | `com.sdem.city.SmartCityApplication` |
| sdem-api-gateway | `com.sdem.gateway.ApiGatewayApplication` |
| front-end-app | `com.sdem.web.FrontEndApplication` |

Tip: after the first run, open the **Services** tool window (View > Tool Windows > Services) to start and stop
all six Spring Boot apps from one place.

**Eclipse / STS:** File > Import > Existing Maven Projects, select the root folder, then right click each main class
> Run As > Spring Boot App (or Java Application) in the same order.

---

## 7. Option C: run with Docker Compose

No Java or Maven needed; the images are built inside Docker.

```bash
docker compose up --build
```

The first build takes several minutes. Compose starts the registry first, waits until it is healthy, then starts the
other services. All ports are published to `localhost` exactly as in the table above.

```bash
docker compose ps          # see container status
docker compose logs -f citizen-app    # follow one service's logs
docker compose down        # stop and remove everything
```

---

## 8. Check that it works

### 8.1 In the browser

| What | URL | What you should see |
|---|---|---|
| Web application | http://localhost:8090 | City dashboard with totals, provider and district tables |
| Eureka dashboard | http://localhost:8761 | 4 registered apps: `SDEM-API-GATEWAY`, `ELECTRICITY-PROVIDER-APP`, `CITIZEN-APP`, `SMART-CITY-APP` |
| Gateway routes | http://localhost:8080/actuator/gateway/routes | JSON list of the three routes |
| Provider Swagger UI | http://localhost:8081/swagger-ui.html | Interactive API docs |
| Citizen Swagger UI | http://localhost:8082/swagger-ui.html | Interactive API docs |
| Smart city Swagger UI | http://localhost:8083/swagger-ui.html | Interactive API docs |

### 8.2 Quick demo in the web application

1. Open **http://localhost:8090**: the dashboard shows 6 seeded households across 4 providers.
2. Go to **Citizens** and click **View** on *Amelia Clarke*: you see her readings, consumption and bill.
3. Submit a new meter reading higher than the last one: consumption and bill update.
4. Try a lower reading: you get a clear validation message.
5. Switch her to *GreenGrid Energy* (100% renewable), then go back to the dashboard: the renewable share and CO2
   avoided go up.
6. Go to **Providers** and add a new provider; register a new citizen with it on the **Citizens** page.
7. Click **Save snapshot** on the dashboard to store the current city figures.

### 8.3 With curl (through the gateway)

```bash
# Providers
curl http://localhost:8080/api/providers
curl http://localhost:8080/api/providers/cheapest
curl "http://localhost:8080/api/providers/1/quote?kwh=250&days=30"

# Citizens
curl http://localhost:8080/api/citizens
curl http://localhost:8080/api/citizens/1/consumption
curl http://localhost:8080/api/citizens/1/bill

# Register a citizen
curl -X POST http://localhost:8080/api/citizens \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Liam Walker","email":"liam.walker@example.com","postcode":"NE1 8ST","meterId":"SM-3001","providerId":2}'

# Submit a meter reading (use the id returned above)
curl -X POST http://localhost:8080/api/citizens/7/readings \
  -H "Content-Type: application/json" \
  -d '{"readingKwh": 1200.50}'

# Smart city report
curl http://localhost:8080/api/city/summary
curl -X POST http://localhost:8080/api/city/snapshots
curl http://localhost:8080/api/city/snapshots
```

On Windows PowerShell use `curl.exe` instead of `curl`, or use the Postman collection below.

### 8.4 With Postman

Import `postman/SDEM.postman_collection.json` (File > Import). It contains every endpoint, already pointed at the
gateway through the `baseUrl` variable (`http://localhost:8080`).

### 8.5 Health checks

```bash
curl http://localhost:8761/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8090/actuator/health
```

Each should return `{"status":"UP"}`.

---

## 9. Viewing the databases (H2 console)

Each business service has its own in-memory H2 database with a web console:

| Service | Console URL | JDBC URL |
|---|---|---|
| electricity-provider-app | http://localhost:8081/h2-console | `jdbc:h2:mem:providerdb` |
| citizen-app | http://localhost:8082/h2-console | `jdbc:h2:mem:citizendb` |
| smart-city-app | http://localhost:8083/h2-console | `jdbc:h2:mem:citydb` |

User name `sa`, password empty. Because the databases are in memory, **data resets every time a service restarts**
and the seed data is loaded again from `src/main/resources/data.sql`.

---

## 10. Running the tests

```bash
mvn test                                   # all modules
mvn -pl citizen-app test                   # one module
mvn -pl citizen-app -Dtest=CitizenServiceTest test   # one test class
```

Test reports are written to `<module>/target/surefire-reports/`. Tests do not need Eureka or any other service
to be running: downstream calls are mocked and Eureka is disabled in the test context.

---

## 11. Configuration

Every setting lives in `src/main/resources/application.yml` of each module. Common ones can be overridden with
environment variables or `--key=value` arguments, without editing files:

| Setting | Default | Override example |
|---|---|---|
| Eureka address (all clients) | `http://localhost:8761/eureka` | `EUREKA_URL=http://registry:8761/eureka` |
| Gateway address (front end) | `http://localhost:8080` | `SDEM_GATEWAY_URL=http://gateway:8080` |
| Any service port | see table above | `java -jar citizen-app.jar --server.port=9082` |
| CO2 factor (smart city) | `0.207` kg/kWh | `--sdem.grid-carbon-kg-per-kwh=0.19` |

**Running a second instance** of a service (to see load balancing):

```bash
java -jar citizen-app/target/citizen-app.jar --server.port=8092
```

It registers with Eureka under the same name, and the gateway shares requests between both instances.

---

## 12. Troubleshooting

| Problem | Likely cause | Fix |
|---|---|---|
| `503 Service Unavailable` from the gateway | Services have not registered yet | Wait about 30 seconds after start up, then retry. Check http://localhost:8761. |
| `Port 8080 was already in use` | Another app is using the port | Stop that app, or start the service with `--server.port=<free port>` (and update callers). On macOS find it with `lsof -i :8080`; on Windows `netstat -ano \| findstr :8080`. |
| `Cannot execute request on any known server` in logs | Registry not running | Start `service-registry-app` first. |
| Front end says "The SDEM API gateway could not be reached" | Gateway is not running | Start `sdem-api-gateway`. |
| Bill shows "Electricity Provider service is unavailable" | Provider service is down | Start `electricity-provider-app`. |
| `invalid target release: 17` during build | Maven is using an older JDK | Point `JAVA_HOME` at JDK 17 (`export JAVA_HOME=$(/usr/libexec/java_home -v 17)` on macOS). |
| `mvn: command not found` | Maven not installed or not on PATH | Install Maven, or use Option B (IntelliJ) or Option C (Docker). |
| My new data disappeared | H2 is in-memory | Expected: data resets on restart. |
| Docker build is very slow the first time | Dependencies are downloading | Normal; later builds use the cache. |

---

## 13. Stopping everything

| Started with | Stop with |
|---|---|
| Helper script | `./scripts/stop-all.sh` |
| Separate terminals | `Ctrl + C` in each terminal |
| IntelliJ | Red ■ stop button (or Stop All in the Services window) |
| Docker Compose | `docker compose down` |
