#!/usr/bin/env bash

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR=${SCRIPT_DIR}

echo "Current directory: ${PROJECT_DIR}"

cd ${PROJECT_DIR}/ui
rm -rf dist/
echo "Clean the dist folder of UI"
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

case $CMD in
  build)
      ./gradlew clean build
      ;;
  run)
      ./gradlew clean bootRun
      ;;
esac

echo "End"