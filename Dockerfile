FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/slotsswapper-backend-0.0.1-SNAPSHOT.jar /app/slotsswapper-backend-0.0.1-SNAPSHOT.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","slotsswapper-backend-0.0.1-SNAPSHOT.jar"]