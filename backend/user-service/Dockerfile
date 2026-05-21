FROM maven:3.9.11-eclipse-temurin-25 AS builder

WORKDIR /workspace

COPY pom.xml .
COPY backend/eureka-server/pom.xml backend/eureka-server/pom.xml
COPY backend/config-server/pom.xml backend/config-server/pom.xml
COPY backend/user-service/pom.xml backend/user-service/pom.xml
COPY backend/game-service/pom.xml backend/game-service/pom.xml
COPY backend/analysis-service/pom.xml backend/analysis-service/pom.xml
RUN mvn -pl backend/user-service -am dependency:go-offline -B

COPY backend/user-service/src backend/user-service/src
RUN mvn -pl backend/user-service -am clean package -DskipTests -B

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/backend/user-service/target/user-service-*.jar app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
