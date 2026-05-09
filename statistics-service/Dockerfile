FROM eclipse-temurin:8-jre
LABEL maintainer="Alexander Lukyanchikov <sqshq@sqshq.com>"

COPY ./target/statistics-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/statistics-service.jar"]

EXPOSE 7000