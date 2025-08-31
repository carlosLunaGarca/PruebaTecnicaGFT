
# Multi-stage build for production
FROM eclipse-temurin:17-jdk-jammy AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and dependencies
COPY gradle/ gradle/
COPY gradlew build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x ./gradlew

# Download dependencies (this layer is cached if dependencies don't change)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src/ src/

# Build the application (skip tests for production build)
RUN ./gradlew clean build -x test --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-jammy

# Create non-root user for security
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the JAR file from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Install curl and download the certificate
USER root
RUN apt-get update && \
    apt-get install -y curl openjdk-17-jre-headless && \
    mkdir -p /etc/ssl/certs/aws-docdb && \
    curl -sS "https://truststore.pki.rds.amazonaws.com/global/global-bundle.pem" -o /etc/ssl/certs/aws-docdb/global-bundle.pem && \
    chmod 644 /etc/ssl/certs/aws-docdb/global-bundle.pem && \
    keytool -import -noprompt -alias rds-global-bundle -file /etc/ssl/certs/aws-docdb/global-bundle.pem -keystore /etc/ssl/cacerts -storepass changeit && \
    chmod 644 /etc/ssl/cacerts && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Change ownership to appuser
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose application port
EXPOSE 8080

# JVM optimization parameters
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djavax.net.ssl.trustStore=/etc/ssl/cacerts -Djavax.net.ssl.trustStorePassword=changeit"

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
