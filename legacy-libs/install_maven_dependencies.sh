#!/bin/sh
# To make this project work with maven, execute this batch file to install dependencies which are not in the central repository

#JAVA_HOME='/c/Program Files/Java/jdk-11.0.8'
#echo ${JAVA_HOME}
#PATH=${JAVA_HOME}/bin:${PATH}
##
## Install the other dependencies that exist locally
mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-sdk -Dversion=6.3.0 -Dpackaging=jar -Dfile=dps-sdk-6.3.0.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=commons -Dversion=3.2 -Dpackaging=jar -Dfile=Common-3.2.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=indigo -Dversion=3.1 -Dpackaging=jar -Dfile=Indigo-3.1.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=metswriter -Dversion=3.2 -Dpackaging=jar -Dfile=ExLibrisMetsWriter-3.2.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=deposit -Dversion=5.0 -Dpackaging=jar -Dfile=DepositWS-5.0.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=metsxmlbeans -Dversion=1.9 -Dpackaging=jar -Dfile=metsxmlbeans-1.9.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=producerws -Dversion=5.0 -Dpackaging=jar -Dfile=ProducerWS-5.0.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=sipws -Dversion=5.0 -Dpackaging=jar -Dfile=SipWS-5.0.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=srusearchclient -Dversion=1.16 -Dpackaging=jar -Dfile=srusearchclient-1.16.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=deliveryaccessws -Dversion=5.3 -Dpackaging=jar -Dfile=DeliveryAccessWS-5.3.jar
mvn install:install-file -DgroupId=com.exlibris -DartifactId=RosettaIEMetaDataParser -Dversion=1.7 -Dpackaging=jar -Dfile=RosettaIEMetaDataParser-1.7.jar
mvn install:install-file -DgroupId=marcxml -DartifactId=marcxml -Dversion=1.0.0 -Dpackaging=jar -Dfile=marcxml-1.0.0.jar
mvn install:install-file -DgroupId=xmlbeans -DartifactId=xmlpublic -Dversion=2.3.0 -Dpackaging=jar -Dfile=xmlpublic-2.3.0.jar
