# Use Java 24
FROM eclipse-temurin:24-jdk as builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper files
COPY mvnw mvnw
COPY .mvn .mvn

# Copy project files
COPY pom.xml .
COPY src ./src

# Make mvnw executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Run stage
FROM eclipse-temurin:24-jdk
WORKDIR /app

# Copy jar from build stage
COPY --from=builder /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]
