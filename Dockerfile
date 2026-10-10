# One recipe for every runnable module. Choose the module with the MODULE build arg:
#   docker build --build-arg MODULE=main-program -t sdem/main-program .

# ---- Build stage -----------------------------------------------------------
FROM maven:3.9-eclipse-temurin-17 AS build
ARG MODULE
WORKDIR /workspace

# Copy the POMs first so dependency downloads are cached between builds
COPY pom.xml .
COPY service-registry-app/pom.xml service-registry-app/
COPY sdem-api-gateway/pom.xml sdem-api-gateway/
COPY electricity-provider-app/pom.xml electricity-provider-app/
COPY smart-city-app/pom.xml smart-city-app/
COPY citizen-app/pom.xml citizen-app/
COPY main-program/pom.xml main-program/
COPY unit-tests/pom.xml unit-tests/
COPY front-end-app/pom.xml front-end-app/
COPY integration-tests/pom.xml integration-tests/
RUN mvn -B -q -pl ${MODULE} -am dependency:go-offline || true

COPY . .
RUN mvn -B -q -pl ${MODULE} -am package -DskipTests \
 && if [ -f ${MODULE}/target/${MODULE}-exec.jar ]; then cp ${MODULE}/target/${MODULE}-exec.jar /workspace/app.jar; \
    else cp ${MODULE}/target/${MODULE}.jar /workspace/app.jar; fi

# ---- Run stage -------------------------------------------------------------
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*
COPY --from=build /workspace/app.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
