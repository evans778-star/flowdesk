FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/flowdesk-1.0.0-SNAPSHOT.jar /app/flowdesk.jar

EXPOSE 8888 9000

ENTRYPOINT ["java", "-jar", "/app/flowdesk.jar"]
