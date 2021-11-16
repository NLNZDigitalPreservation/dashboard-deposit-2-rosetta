rem call mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-sdk -Dversion=5.0.1 -Dpackaging=jar -Dfile=dps-sdk-5.0.1.jar
call mvn install:install-file -DgroupId=nz.govt.natlib.ndha -DartifactId=commons -Dversion=3.2 -Dpackaging=jar -Dfile=./legacy-libs/Common-3.2.jar
call mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-sdk -Dversion=7.1.0 -Dpackaging=jar -Dfile=./legacy-libs/dps-sdk-7.1.0.jar
call mvn install:install-file -DgroupId=com.exlibris -DartifactId=dps-sdk-deposit-api -Dversion=7.1.0 -Dpackaging=jar -Dfile=./legacy-libs/dps-sdk-deposit-api-7.1.0.jar