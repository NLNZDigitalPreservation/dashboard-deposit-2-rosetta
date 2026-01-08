# Stage 1: Build the Vue project
FROM node:20-slim AS build

RUN apt-get update && \
    apt-get install -y ca-certificates git openjdk-17-jdk maven

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Add host certificates using BuildKit secret
# RUN --mount=type=secret,id=host-certs,target=/usr/local/share/ca-certificates/host.crt \
#     update-ca-certificates

# RUN --mount=type=secret,id=host-certs,target=/tmp/host.crt \
#     keytool -importcert -trustcacerts -alias host \
#         -file /tmp/host.crt \
#         -keystore ${JAVA_HOME}/lib/security/cacerts \
#         -storepass changeit \
#         -noprompt

# Set workdir
WORKDIR /deployment

RUN git config --global http.sslVerify false && \
    git clone --branch containerize2 --depth 1 https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta.git dashboard

WORKDIR /deployment/dashboard

RUN npm config set strict-ssl false && \
    bash build.sh && \
    mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -DskipTests


EXPOSE 1901

# FROM eclipse-temurin:17-jdk
# FROM docker.io/eclipse-temurin:17-jdk-alpine
# FROM docker.io/azul/zulu-openjdk-alpine:17-jre-headless
FROM docker.io/eclipse-temurin:17-jre-alpine


# Create deployment folder in final image
WORKDIR /deployment

# Copy the WAR from the build stage
COPY --from=build /deployment/dashboard/target/deposit-dashboard-*.war ./dashboard.war

# Optional: if using Tomcat or running with java -jar
# You can adjust ENTRYPOINT accordingly
ENTRYPOINT ["java", "-jar", "/deployment/dashboard.war"]



