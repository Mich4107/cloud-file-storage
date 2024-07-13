FROM gradle:8.9.0-jdk21-alpine
WORKDIR /app
COPY . .
RUN gradle build -x test
RUN cp build/libs/cloud_file_storage.jar app.jar
CMD ["java", "-jar", "app.jar"]