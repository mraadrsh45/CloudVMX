# STAGE 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml and install dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# STAGE 2: Run the application
FROM eclipse-temurin:21-jre

# Install X11 libraries required for JavaFX and other GUI components
RUN apt-get update && apt-get install -y libxext6 libxrender1 libxtst6 libxi6 && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

# Define default command
ENTRYPOINT ["java", "-jar", "app.jar"]
