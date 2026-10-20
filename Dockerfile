# syntax=docker/dockerfile:1
# =============================================================================
#  Dockerfile - CineX (Spring Boot 3.5 / Java 17 / Gradle)
# =============================================================================

# ---------- Stage 1: build - biên dịch & đóng gói bằng Gradle + JDK 17 ----------
FROM gradle:8.14-jdk17-alpine AS build
WORKDIR /workspace

# Copy build script trước để tận dụng cache layer của dependency
COPY build.gradle settings.gradle gradle.properties ./
RUN gradle dependencies --no-daemon -q

COPY src ./src
RUN gradle bootJar -x test --no-daemon -q

# ---------- Stage 2: runtime - chỉ chứa JRE 17 + file JAR ----------
FROM eclipse-temurin:17-jre-alpine AS runtime

ARG APP_VERSION=dev
ENV APP_VERSION=${APP_VERSION} \
    JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=build --chown=spring:spring /workspace/build/libs/cinex.jar /app/cinex.jar
USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/cinex.jar"]
