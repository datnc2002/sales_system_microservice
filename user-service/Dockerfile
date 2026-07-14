# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy parent POM and common-lib first (cache dependencies)
COPY pom.xml ./pom.xml
COPY common-lib/pom.xml ./common-lib/pom.xml
COPY user-service/pom.xml ./user-service/pom.xml
COPY product-service/pom.xml ./product-service/pom.xml
COPY order-service/pom.xml ./order-service/pom.xml
COPY inventory-service/pom.xml ./inventory-service/pom.xml
COPY notification-service/pom.xml ./notification-service/pom.xml
COPY api-gateway/pom.xml ./api-gateway/pom.xml

# Download dependencies
RUN mvn dependency:go-offline -pl common-lib,user-service -am -B

# Copy source code
COPY common-lib/src ./common-lib/src
COPY user-service/src ./user-service/src

# Build
RUN mvn clean package -pl common-lib,user-service -am -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/user-service/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
