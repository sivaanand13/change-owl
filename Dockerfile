FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY changeowl-shared/ ./changeowl-shared/

ARG SERVICE_NAME=github-ingestion-service
COPY services/${SERVICE_NAME}/ ./services/${SERVICE_NAME}/

RUN mvn clean install -DskipTests -pl changeowl-shared -am
RUN mvn clean package -DskipTests -pl services/${SERVICE_NAME}

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ARG SERVICE_NAME=github-ingestion-service

COPY --from=build /app/services/${SERVICE_NAME}/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]