FROM eclipse-temurin:8-jre
LABEL maintainer="Chi Dov <d.chiproeng@gmail.com>"

COPY ./target/turbine-stream-service.jar /app/
CMD ["java", "-Xmx200m", "-jar", "/app/turbine-stream-service.jar"]

EXPOSE 8989