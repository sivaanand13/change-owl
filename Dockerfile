FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY changeowl-shared/ ./changeowl-shared/

# 2. Copy the service you want to build (e.g., github-ingestion-service)
# We define an ARG so you can reuse this Dockerfile for any service
ARG SERVICE_NAME=github-ingestion-service
COPY services/${SERVICE_NAME}/ ./services/${SERVICE_NAME}/

# 3. Install the parent and shared module, then build the specific service
# This ensures changeowl-shared is in the local repo for the service to find
RUN mvn clean install -DskipTests -pl changeowl-shared -am
RUN mvn clean package -DskipTests -pl services/${SERVICE_NAME}

# Stage 2: Runtime Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ARG SERVICE_NAME=github-ingestion-service
# Copy the built jar from the build stage
COPY --from=build /app/services/${SERVICE_NAME}/target/*.jar app.jar

# Standard Spring Boot port (adjust per service if needed)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]