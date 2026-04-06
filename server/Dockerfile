# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy Gradle wrapper + build metadata first for caching
COPY settings.gradle.kts build.gradle.kts gradle.properties gradlew gradlew.bat /workspace/
COPY gradle /workspace/gradle
COPY server/build.gradle.kts /workspace/server/build.gradle.kts
COPY shared/build.gradle.kts /workspace/shared/build.gradle.kts

# Fetch dependencies using the repo's Gradle wrapper (pins the Gradle version)
RUN ./gradlew --no-daemon :server:dependencies > /dev/null

# Now copy sources
COPY shared /workspace/shared
COPY server /workspace/server

# Build an application distribution with startup scripts + libs
RUN ./gradlew --no-daemon :server:installDist


# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Ktor listens on 8080 by default; our code uses SERVER_PORT (see Application.kt)
ENV PORT=8080

# Copy the Gradle installDist output
COPY --from=build /workspace/server/build/install/server /app

# Non-root user for safety
RUN useradd -r -u 10001 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

# The installDist script is named after the Gradle app (defaults to project name: "server")
CMD ["/app/bin/server"]
