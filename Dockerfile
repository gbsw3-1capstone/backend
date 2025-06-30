FROM gradle:jdk17-focal AS build
WORKDIR /home/gradle/project
COPY . .
RUN gradle bootJar

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /home/gradle/project/build/libs/*.jar ./app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]