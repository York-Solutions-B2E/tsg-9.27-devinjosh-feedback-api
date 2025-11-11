    # ---- Build stage ------------------------------------------------------------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Only copy pom first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -e -DskipTests dependency:go-offline

# Now copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests package

# ---- Runtime stage ----------------------------------------------------------
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as a non-root user
RUN useradd spring
USER spring

# Copy the built jar (ARG lets the wildcard resolve at build time)
ARG JAR_FILE=/app/target/*.jar
COPY --from=build ${JAR_FILE} /app/app.jar

# Optional: put your app.yml in /config if you want to override defaults
# COPY application.yml /app/config/application.yml

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=default", "-jar", "/app/app.jar"]
