FROM openjdk:17-jdk-alpine3.14
COPY target/Scentbird_tic_tac_toe-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]