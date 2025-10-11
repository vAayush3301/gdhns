# ===== Stage 1: Build the project with Gradle =====
FROM gradle:8.3-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy all source files
COPY . .

# Build the project (skip tests for faster build)
RUN gradle clean build -x test

# ===== Stage 2: Create lightweight runtime image =====
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose the port your Spring Boot app runs on
EXPOSE 8080

# Environment variable for Firebase service account JSON
# (Render will set this in the dashboard)
ENV GOOGLE_APPLICATION_CREDENTIALS_JSON=""

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
