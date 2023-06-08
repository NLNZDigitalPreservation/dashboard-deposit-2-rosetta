FROM ubuntu:22.04 AS base

# ARG HTTP_PROXY_HOST=webaccess.dia.govt.nz
# ARG HTTP_PROXY_PORT=8080
ARG HTTP_PROXY_HOST=127.0.0.1
ARG HTTP_PROXY_PORT=3128
# ARG HTTP_PROXY_HOST=zproxyforservers.dia.govt.nz
# ARG HTTP_PROXY_PORT=443
ARG HTTP_PROXY_SERVER=http://$HTTP_PROXY_HOST:$HTTP_PROXY_PORT/

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y --no-install-recommends \
        openjdk-11-jdk git maven

# ENV http_proxy=http://$HTTP_PROXY_HOST:$HTTP_PROXY_PORT/
# ENV https_proxy=http://$HTTP_PROXY_HOST:$HTTP_PROXY_PORT/
RUN git config --global http.proxy $HTTP_PROXY_SERVER && \
    git config --global https.proxy $HTTP_PROXY_SERVER


WORKDIR /root/deployment
RUN mkdir dashboard && \
    git clone https://github.com/NLNZDigitalPreservation/dashboard-deposit-2-rosetta.git dashboard && \   
    cd ./dashboard && \
    git checkout origin/containerize && \
    cp ./settings.xml /etc/maven/settings.xml && \
    ./install_maven_dependencies.sh && \
    ./gradlew -Dhttp.proxyHost=$HTTP_PROXY_HOST -Dhttp.proxyPort=$HTTP_PROXY_PORT -Dhttps.proxyHost=$HTTP_PROXY_HOST -Dhttps.proxyPort=$HTTP_PROXY_PORT clean build -x test

WORKDIR /root/dashboard
RUN rm /root/deployment/dashboard/build/libs/dashboard-*-plain.war && \
    mv /root/deployment/dashboard/build/libs/dashboard*.war ./dashboard.war && \
    rm -rf /root/deployment


# Recreate a clean image without the building tools
# FROM ubuntu:22.04 AS final

#RUN apt-get update && \
#    apt-get install -y --no-install-recommends \
#    openjdk-11-jdk-headless

#WORKDIR /root/deployment

#COPY --from=base ./dashboard/build/libs/dashboard*.war /root/deployment/dashboard.war


ENTRYPOINT ["java", "-jar", "dashboard.war"]
