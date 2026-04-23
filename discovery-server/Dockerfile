# Stage 1: Build
FROM gradle:8.6-jdk17 AS builder
WORKDIR /app
COPY . .
RUN gradle build

# Stage 1: Run
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "app.jar"]