FROM eclipse-temurin:8-jre
LABEL maintainer="Alexander Lukyanchikov <sqshq@sqshq.com>"

COPY ./target/account-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/account-service.jar"]

EXPOSE 6000