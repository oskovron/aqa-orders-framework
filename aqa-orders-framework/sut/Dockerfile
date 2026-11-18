# Runtime image for orders-service.
# The jar is built by Gradle first:  ./gradlew :sut:bootJar  ->  sut/build/libs/orders-service.jar
FROM eclipse-temurin:21-jre

# curl is used by the compose health check (GET /actuator/health)
RUN apt-get update \
 && apt-get install -y --no-install-recommends curl \
 && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build/libs/orders-service.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]