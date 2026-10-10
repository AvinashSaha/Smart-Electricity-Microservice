# One recipe for every SDEM module. Choose the module with the MODULE build arg:
#   docker build --build-arg MODULE=citizen-app -t sdem/citizen-app .

# ---- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /workspace

# Copy POMs first so dependency downloads are cached between builds
COPY pom.xml .
COPY service-registry-app/pom.xml service-registry-app/
COPY sdem-api-gateway/pom.xml sdem-api-gateway/
COPY electricity-provider-app/pom.xml electricity-provider-app/
COPY citizen-app/pom.xml citizen-app/
COPY smart-city-app/pom.xml smart-city-app/
COPY front-end-app/pom.xml front-end-app/
RUN mvn -B -q -pl ${MODULE} -am dependency:go-offline

COPY . .
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests

# ---- Run stage -------------------------------------------------------------
FROM eclipse-temurin:17-jre
ARG MODULE
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/${MODULE}/target/${MODULE}.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
