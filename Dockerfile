# ==============================================================================
# Multi-Stage Dockerfile for Spring Boot 3 (Java 21)
# Optimized for Cloud Deployment (Render, Railway, Fly.io)
# ==============================================================================

# ------------------------------------------------------------------------------
# Stage 1: Build the Application
# ------------------------------------------------------------------------------
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# 1. Cache Maven dependencies by copying only pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B || true

# 2. Copy source code and compile optimized production JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# ------------------------------------------------------------------------------
# Stage 2: Minimal Runtime Environment
# ------------------------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Security: Create and use a dedicated non-root application user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy the compiled JAR artifact from the builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar app.jar

# Switch to non-root user
USER appuser

# JVM Memory and Container optimizations for Render's free/starter tier (512MB RAM)
ENV PORT=8080 \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

# Document exposed port (Render dynamically binds $PORT)
EXPOSE 8080

# Execute Spring Boot application passing Render's dynamic $PORT
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT} -jar app.jar"]
