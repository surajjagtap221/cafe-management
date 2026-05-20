# Stage 1 — Build .jar with Maven
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2 — Run .jar in lightweight Java container
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN mkdir -p /app/cafe-stored-files
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
