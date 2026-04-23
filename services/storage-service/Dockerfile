FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY changeowl-shared/ ./changeowl-shared/
COPY services/ ./services/

RUN mvn clean install -DskipTests -pl changeowl-shared -am
RUN mvn clean package -DskipTests -pl services/storage-service

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/services/storage-service/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]