# Etapa de build
FROM gradle:8.5-jdk17 AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle build --no-daemon

# Etapa de execução
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
