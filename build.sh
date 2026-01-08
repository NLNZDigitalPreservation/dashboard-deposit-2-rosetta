#!/usr/bin/env bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR=${SCRIPT_DIR}

echo "Current directory: ${PROJECT_DIR}"

cd ${PROJECT_DIR}/ui
rm -rf dist/
echo "Cleaned the dist folder of UI"
npm install
echo "Installed the dependencies of UI"
npm audit fix
echo "Audited and fixed the dependencies of UI"
npm run build-only
echo "Built the UI"

cd ${PROJECT_DIR}/src/main/resources
rm -rf static/
mkdir static
echo "Cleaned the static files of Java"
cp -R ${PROJECT_DIR}/ui/dist/* ./static
echo "Copied the dist files to the static folder"

cd ${PROJECT_DIR}

CMD=$1

export GRADLE_OPTS="-Dorg.gradle.internal.http.socketTimeout=60000 -Dorg.gradle.internal.http.connectionTimeout=60000 -Djavax.net.ssl.trustStoreType=JKS -Djavax.net.ssl.trustStore=/dev/null -Djavax.net.ssl.trustStorePassword=changeit"

case $CMD in
  build)
      ./gradlew clean build
      ;;
  run)
      ./gradlew clean bootRun
      ;;
  *)
      mvn clean package -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true -DskipTests
      ;;
esac

echo "End"