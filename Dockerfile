# ---- Stage 1: Build the application ----
FROM gradle:8.5-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper and project files first (for better caching)
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle gradle
RUN ./gradlew dependencies --no-daemon || return 0

# Copy source code
COPY src src

# Build the Spring Boot JAR
RUN ./gradlew clean bootJar --no-daemon

# ---- Stage 2: Run the application ----
FROM eclipse-temurin:17-jdk

# Set working directory
WORKDIR /app

# JVM tuning options
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError -Xms512m -Xmx1024m -XX:+ExitOnOutOfMemoryError"

# Copy built JAR from the build stage
COPY --from=build /app/build/libs/flashlink-url-service-*.jar app.jar

# Expose application port
EXPOSE 8080

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
