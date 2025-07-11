FROM eclipse-temurin:21-jdk-alpine as build

# Install Node.js for Vaadin frontend preparation
RUN apk add --no-cache nodejs npm

WORKDIR /workspace/app

# Copy Maven files first for better layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Copy source code
COPY src src
COPY frontend frontend

# Build the application with Vaadin production profile
RUN ./mvnw clean package -Pproduction -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy the built artifact from build stage
COPY --from=build /workspace/app/target/*.jar app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
