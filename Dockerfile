FROM gradle:8.3-jdk17 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY gradle gradle
RUN gradle dependencies --no-daemon || return 0
COPY src src
RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
