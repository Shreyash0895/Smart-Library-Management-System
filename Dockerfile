# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy source files
COPY . /app

# Download MongoDB Java driver (assuming it's not included)
RUN apt-get update && apt-get install -y wget && \
    wget -O mongodb-driver.jar https://repo1.maven.org/maven2/org/mongodb/mongodb-driver-sync/4.9.1/mongodb-driver-sync-4.9.1.jar && \
    wget -O bson.jar https://repo1.maven.org/maven2/org/mongodb/bson/4.9.1/bson-4.9.1.jar

# Compile the Java application
RUN javac -cp ".:mongodb-driver.jar:bson.jar" *.java

# Expose port if needed (though it's a desktop app, maybe for future web version)
# EXPOSE 8080

# Run the application (this will fail in container without display, but for demo)
CMD ["java", "-cp", ".:mongodb-driver.jar:bson.jar", "LoginScreen"]