# Stage 1: Build the Vue project
FROM node:20-slim AS ui-build

RUN apt-get update && \
    apt-get install -y ca-certificates git openjdk-17-jdk

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Add host certificates using BuildKit secret
RUN --mount=type=secret,id=host-certs,target=/usr/local/share/ca-certificates/host.crt \
    update-ca-certificates

RUN --mount=type=secret,id=host-certs,target=/tmp/host.crt \
    keytool -importcert -trustcacerts -alias host \
        -file /tmp/host.crt \
        -keystore ${JAVA_HOME}/lib/security/cacerts \
        -storepass changeit \
        -noprompt

# Set workdir
WORKDIR /deployment

RUN git config --global http.sslVerify false && \
    git clone --depth 1 https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta.git dashboard

WORKDIR /deployment/dashboard

RUN npm config set strict-ssl false && \
    bash build.sh




# # RUN rm -rf ./dist ./node_modules ./package-lock.json
# RUN npm install
# RUN npm run build



# FROM eclipse-temurin:17.0.17_10-jdk-alpine-3.23 AS gradle-build


# RUN apk update && apk add --no-cache git
# # Set workdir
# WORKDIR /deployment

# RUN git clone --depth 1 https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta.git dashboard
# WORKDIR /deployment/dashboard

# RUN ./gradlew clean build




