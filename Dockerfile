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


# Stage 2: Build the web server project
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu AS build-server
WORKDIR /build
COPY ./ ./

WORKDIR /build/src/main/resources
RUN rm -rf static

COPY --from=build-ui /build/ui/dist ./static

WORKDIR /build

RUN rm -rf certs && \
    mkdir certs && \
    tar -xvf certs.tar.gz -C ./ && \
    keytool -importcert -alias ZscalerRootCA -cacerts -storepass changeit -file certs/ZscalerRoot.crt -noprompt && \
    ./gradlew clean build -x test && \
    mv ./build/libs/dashboard-*.war ./ && \
    rm -rf certs certs.tar.gz


# Stage 3: Build the final project
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# Create deployment folder in final image
WORKDIR /deployment

COPY ./certs.tar.gz ./certs.tar.gz

RUN rm -rf certs && \
    mkdir certs && \
    tar -xvf certs.tar.gz -C ./ && \
    keytool -importcert -alias rootCA -cacerts -storepass changeit -file certs/NZGovtCA004.crt -noprompt && \
    keytool -importcert -alias intermediateCA -cacerts -storepass changeit -file certs/NZGovtCA205.crt -noprompt && \
    keytool -importcert -alias natlibECC -cacerts -storepass changeit -file certs/NZGovtCA342.crt -noprompt && \
    keytool -importcert -alias rootRSA -cacerts -storepass changeit -file certs/NZGovtCA003.crt -noprompt && \
    keytool -importcert -alias intermediateRSA -cacerts -storepass changeit -file certs/NZGovtCA204.crt -noprompt && \
    keytool -importcert -alias natlibRSA -cacerts -storepass changeit -file certs/NZGovtCA338.crt -noprompt && \
    keytool -importcert -alias ZscalerRootCA -cacerts -storepass changeit -file certs/ZscalerRoot.crt -noprompt && \
    rm -rf certs certs.tar.gz

# Copy the WAR from the build stage
COPY --from=build-server /build/dashboard-*.war ./dashboard.war

# Optional: if using Tomcat or running with java -jar
# You can adjust ENTRYPOINT accordingly
ENTRYPOINT ["java", "-jar", "/deployment/dashboard.war"]
