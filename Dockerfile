FROM ubuntu:22.04 as build

RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        openjdk-17-jdk git maven

WORKDIR /root/deployment
RUN mkdir dashboard && \
    git clone https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta.git dashboard && \   
    cd ./dashboard && \
    git checkout origin/containerize && \
    ./install_maven_dependencies.sh && \
    ./gradlew clean build -x test && \
    ls -l  /root/deployment/dashboard/build/libs

# Recreate a clean image without the building tools
FROM openjdk:17-jdk-alpine
COPY --from=build /root/deployment/dashboard/build/libs/ /root/deployment/
WORKDIR /root/deployment
RUN ls -al /root && \
    java -version

ENTRYPOINT ["sleep", "3000"]
