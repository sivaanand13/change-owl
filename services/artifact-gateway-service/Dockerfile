FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY changeowl-shared/ ./changeowl-shared/
COPY services/ ./services/

RUN mvn -DskipTests clean package -pl services/artifact-gateway-service -am

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/services/artifact-gateway-service/target/*.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]