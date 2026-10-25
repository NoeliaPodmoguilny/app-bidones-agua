
FROM eclipse-temurin:23-jre-alpine
WORKDIR /bidones
ARG JAR_FILE=target/bidones-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app_bidones.jar
EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=75", "-XX:TieredStopAtLevel=1", "-jar", "app_bidones.jar"]