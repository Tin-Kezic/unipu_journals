FROM gradle:8.5-jdk17 AS build

WORKDIR /home/gradle/project

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN ./gradlew dependencies --no-daemon || true

COPY src ./src

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:24-jdk-alpine

WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
