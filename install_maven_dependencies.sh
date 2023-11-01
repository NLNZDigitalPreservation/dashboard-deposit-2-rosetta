#!/bin/sh
# To make this project work with maven, execute this batch file to install dependencies which are not in the central repository

#JAVA_HOME='/c/Program Files/Java/jdk-11.0.8'
#echo ${JAVA_HOME}
#PATH=${JAVA_HOME}/bin:${PATH}
##
## Install the other dependencies that exist locally
mvn install:install-file -DgroupId=nz.govt.natlib.ndha -DartifactId=commons -Dversion=3.2 -Dpackaging=jar -Dfile=./legacy-libs/Common-3.2.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-sdk -Dversion=7.1.0 -Dpackaging=jar -Dfile=./legacy-libs/dps-sdk-7.1.0.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-sdk-deposit-api -Dversion=7.1.0 -Dpackaging=jar -Dfile=./legacy-libs/dps-sdk-deposit-api-7.1.0.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-deposit-dao -Dversion=8.1.0 -Dpackaging=jar -Dfile=./legacy-libs/dps-deposit-dao.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-deposit-model -Dversion=8.1.0 -Dpackaging=jar -Dfile=./legacy-libs/dps-deposit-model.jar
