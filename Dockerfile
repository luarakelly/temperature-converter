FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/classes /app/classes

CMD ["java", "-cp", "/app/classes", "TemperatureConverterApp"]
