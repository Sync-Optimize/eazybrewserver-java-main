# --- Stage 1: Build the Application ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy only the pom.xml first (optimizes caching)
COPY pom.xml .
# Download dependencies (this layer will be cached unless pom changes)
RUN mvn dependency:go-offline

# Copy the actual source code
COPY src ./src

# Build the JAR (skip tests to save time during deploy)
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/target/*.jar app.jar

# Expose the port
EXPOSE 9090

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]