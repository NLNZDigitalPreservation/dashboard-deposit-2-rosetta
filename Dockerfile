FROM ubuntu:22.04

ARG HTTP_PROXY_HOST=webaccess.dia.govt.nz
ARG HTTP_PROXY_PORT=8080

RUN apt update && \
    apt upgrade -y && \
    apt install -y --no-install-recommends \
        openjdk-11-jdk git maven curl wget unzip

ENV http_proxy=http://$HTTP_PROXY_HOST:$HTTP_PROXY_PORT/
ENV https_proxy=http://$HTTP_PROXY_HOST:$HTTP_PROXY_PORT/

WORKDIR /root/deployment
RUN wget https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta/archive/refs/heads/main.zip && \
    unzip main.zip && \
    cd ./dashboard-deposit-2-rosetta-main && \
    cp ./settings.xml /etc/maven/settings.xml && \
    ./install_maven_dependencies.sh && \
    ./gradlew -Dhttp.proxyHost=$HTTP_PROXY_HOST -Dhttp.proxyPort=$HTTP_PROXY_PORT -Dhttps.proxyHost=$HTTP_PROXY_HOST -Dhttps.proxyPort=$HTTP_PROXY_PORT clean build -x test

WORKDIR /root/dashboard
RUN rm /root/deployment/dashboard-deposit-2-rosetta-main/build/libs/dashboard-*-plain.war && \
    mv /root/deployment/dashboard-deposit-2-rosetta-main/build/libs/dashboard*.war ./dashboard.war && \
    rm -rf /root/deployment

ENTRYPOINT ["java", "-jar", "dashboard.war"]
