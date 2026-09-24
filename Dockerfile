# =====================================================
# BUILD STAGE
# =====================================================

FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy Maven configuration first
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

RUN chmod +x mvnw

# Download dependencies
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests


# =====================================================
# RUNTIME STAGE
# =====================================================

FROM eclipse-temurin:21-jre

WORKDIR /app

# Create non-root user
RUN groupadd --system spring \
    && useradd --system \
       --gid spring \
       --home-dir /app \
       --shell /usr/sbin/nologin \
       spring

# Copy generated JAR
COPY --from=build /app/target/*.jar app.jar

# Order Service port
EXPOSE 8083

# Run as non-root user
USER spring

# Container-aware JVM settings
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]