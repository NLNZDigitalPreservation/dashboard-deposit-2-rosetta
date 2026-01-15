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
RUN keytool -importcert -alias ZscalerRootCA -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file ZscalerRoot.crt -noprompt && \
    ./gradlew clean build -x test && \
    mv ./build/libs/dashboard-*.war ./ && \
    rm *.crt


# Stage 3: Build the final project
FROM mcr.microsoft.com/openjdk/jdk:17-ubuntu

# Create deployment folder in final image
WORKDIR /deployment

COPY ./*.crt ./
RUN keytool -importcert -alias rootCA -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file NZGovtCA004.crt -noprompt && \
    keytool -importcert -alias intermediateCA -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file NZGovtCA205.crt -noprompt && \
    keytool -importcert -alias natlibECC -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file NZGovtCA342.crt -noprompt && \
    keytool -importcert -alias rootRSA -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file NZGovtCA003.crt -noprompt && \
    keytool -importcert -alias intermediateRSA -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file NZGovtCA204.crt -noprompt && \
    keytool -importcert -alias natlibRSA -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file NZGovtCA338.crt -noprompt && \
    keytool -importcert -alias ZscalerRootCA -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -file ZscalerRoot.crt -noprompt && \
    rm *.crt

# Copy the WAR from the build stage
COPY --from=build-server /build/dashboard-*.war ./dashboard.war

# Optional: if using Tomcat or running with java -jar
# You can adjust ENTRYPOINT accordingly
ENTRYPOINT ["java", "-jar", "/deployment/dashboard.war"]
