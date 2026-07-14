FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml ./pom.xml
COPY common-lib/pom.xml ./common-lib/pom.xml
COPY user-service/pom.xml ./user-service/pom.xml
COPY product-service/pom.xml ./product-service/pom.xml
COPY order-service/pom.xml ./order-service/pom.xml
COPY inventory-service/pom.xml ./inventory-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml
RUN mvn dependency:go-offline -pl common-lib,api-gateway -am -B
COPY common-lib/src ./common-lib/src
COPY api-gateway/src ./api-gateway/src
RUN mvn clean package -pl common-lib,api-gateway -am -DskipTests -B

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/api-gateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
