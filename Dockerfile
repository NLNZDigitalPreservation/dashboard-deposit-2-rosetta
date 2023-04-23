FROM alpine:3.17.3

RUN apk add --no-cache openjdk11 maven curl

WORKDIR /root/deployment
RUN wget https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta/archive/refs/heads/main.zip && \
    unzip main.zip && \
    cd ./dashboard-deposit-2-rosetta-main && \
    ./install_maven_dependencies.sh && \
    ./gradlew clean build -x test

WORKDIR /root/dashboard
RUN rm /root/deployment/dashboard-deposit-2-rosetta-main/build/libs/dashboard-*-plain.war && \
    mv /root/deployment/dashboard-deposit-2-rosetta-main/build/libs/dashboard*.war ./dashboard.war && \
    rm -rf /root/deployment

ENTRYPOINT ["java", "-jar", "dashboard.war"]
