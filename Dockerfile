FROM gradle:8.9.0-jdk21-alpine as build
WORKDIR /app
COPY . .
RUN gradle build -x test

FROM amazoncorretto:21.0.4-al2023-headless
WORKDIR /app
COPY --from=build /app/build/libs/cloud_file_storage.jar app.jar
CMD ["java", "-jar", "app.jar"]