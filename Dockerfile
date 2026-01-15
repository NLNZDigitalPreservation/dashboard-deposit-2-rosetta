# Stage 1: Build the Vue project
FROM node:20-slim AS build-ui
WORKDIR /build
COPY ./ui ./ui
WORKDIR /build/ui

RUN npm config set strict-ssl false && \
    rm -rf dist && \
    npm install && \
    npm audit fix && \
    npm run build-only


# Stage 2: Build the maven project
FROM docker.io/maven:3.9.9-eclipse-temurin-17 AS build-server
WORKDIR /build
COPY ./ ./

WORKDIR /build/src/main/resources
RUN rm -rf static

COPY --from=build-ui /build/ui/dist ./static
WORKDIR /build
RUN mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -DskipTests

# Stage 3: Build the final project
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# Create deployment folder in final image
WORKDIR /deployment

# Copy the WAR from the build stage
COPY --from=build-server /build/target/deposit-dashboard-*.war ./dashboard.war

# Optional: if using Tomcat or running with java -jar
# You can adjust ENTRYPOINT accordingly
ENTRYPOINT ["java", "-jar", "/deployment/dashboard.war"]



