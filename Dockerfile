FROM ubuntu:22.04 AS base

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-17-jdk git maven

WORKDIR /root/deployment
RUN mkdir dashboard && \
    git clone https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta.git dashboard && \   
    cd ./dashboard && \
    git checkout origin/containerize && \
    ./install_maven_dependencies.sh && \
    ./gradlew clean build -x test

# Recreate a clean image without the building tools
FROM openjdk:17-jdk-alpine
WORKDIR /root/deployment

COPY --from=base /root/deployment/dashboard/build/libs/dashboard*.jar /root/deployment/dashboard.jar
RUN ls -l /root/deployment/

ENTRYPOINT ["java", "-jar", "/root/deployment/dashboard.jar"]
