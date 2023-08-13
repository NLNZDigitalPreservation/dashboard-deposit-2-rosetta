FROM alpine:3.18.3

RUN  apk update --no-check-certificate && \
     apk add --no-check-certificate openjdk17

WORKDIR compiled
RUN wget https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta/archive/refs/heads/v2.0.zip --no-check-certificate && \
    unzip v2.0.zip -d /compiled && \
    cd dashboard-deposit* && \
    ./install_maven_dependencies.sh && \
    ./mvnw clean package

WORKDIR deployed
RUN cp /compiled/dashboard-deposit*/*.jar deposit-dashboard.jar && \
    rm -rf /compiled

ENTRYPOINT ["java", "-jar", "deposit-dashboard.jar"]