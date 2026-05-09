FROM eclipse-temurin:8-jre
LABEL maintainer="Alexander Lukyanchikov <sqshq@sqshq.com>"

COPY ./target/gateway.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/gateway.jar"]

EXPOSE 4000