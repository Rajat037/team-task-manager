# Stage 1: Build the React Frontend
FROM node:20-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build the Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21 AS backend-build
WORKDIR /app/backend
COPY backend/pom.xml ./
# Download dependencies first to cache them
RUN mvn dependency:go-offline || true
COPY backend/src ./src
# Copy the built frontend static files into Spring Boot's static resources directory
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
# Build the Spring Boot application
RUN mvn clean package -DskipTests

# Stage 3: Run the Application
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=backend-build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
