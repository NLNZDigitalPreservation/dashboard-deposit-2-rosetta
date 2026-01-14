# Stage 1: Build the Vue project
FROM node:20-slim AS build

RUN apt-get update && \
    apt-get install -y git openjdk-17-jdk maven

ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

# Set workdir
WORKDIR /deployment/dashboard

RUN git config --global http.sslVerify false && \
    git clone --branch containerize2 --depth 1 https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta.git /deployment/dashboard

RUN npm config set strict-ssl false && \
    bash build.sh && \
    mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -DskipTests


EXPOSE 1901


FROM docker.io/eclipse-temurin:17-jre-alpine


# Create deployment folder in final image
WORKDIR /deployment

# Copy the WAR from the build stage
COPY --from=build /deployment/dashboard/target/deposit-dashboard-*.war ./dashboard.war

# Optional: if using Tomcat or running with java -jar
# You can adjust ENTRYPOINT accordingly
#ENTRYPOINT ["java", "-jar", "/deployment/dashboard.war", "--spring.config.location=file:${PERSIST_PATH}/conf/application-ldap.properties"]



