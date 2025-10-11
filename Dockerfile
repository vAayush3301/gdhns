# Use lightweight JDK image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Gradle-built jar into the image
COPY build/libs/main-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run the jar
ENTRYPOINT ["java", "-jar", "app.jar"]
