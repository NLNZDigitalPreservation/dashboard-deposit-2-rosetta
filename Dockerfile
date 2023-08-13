FROM alpine:3.18.3

WORKDIR /compiled
RUN apk add --no-check-certificate openjdk17 && \
    wget https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta/archive/refs/heads/v2.0.zip --no-check-certificate && \
    unzip v2.0.zip -d /compiled && \
    cd dashboard-deposit* && \
    ./install_maven_dependencies.sh && \
    ./mvnw clean package -Dmaven.test.skip=true

WORKDIR /deployment
RUN mv /compiled/dashboard-deposit*/target/*.war deposit-dashboard.war && \
    rm -rf /compiled

ENTRYPOINT ["java", "-jar", "deposit-dashboard.war", "--spring.config.location=file:/deployment/conf/application.properties"]