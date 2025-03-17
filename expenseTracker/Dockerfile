# Use the official OpenJDK 11 image as a parent image
FROM jelastic/maven:3.9.5-openjdk-21 AS build

# Set the working directory in the container
WORKDIR /app

# Copy the pom.xml and any other necessary configuration files
COPY ./pom.xml /app
COPY ./src /app/src

# Build the application
RUN mvn clean package -Dmaven.test.skip=true

# Create a new image for running the application
FROM openjdk:21-jdk

# Set the working directory
WORKDIR /app

# Copy the built JAR file from the previous stage
COPY --from=build /app/target/*.jar expenseTracker-1.0.0-Final.jar

# Make port 8080 available to the world outside this container
EXPOSE 8080

# Run the spring boot application when the container launches
CMD ["java", "-jar", "expenseTracker-1.0.0-Final.jar"]

