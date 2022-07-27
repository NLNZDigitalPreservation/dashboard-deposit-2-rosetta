#!/usr/bin/bash

# Make sure this is running in a screen session
if [ -z "$STY" ]; then exec screen -L -dm -S DepositDashboard /bin/bash "$0";exit 0; fi

# Write a header to the screen session before moving output to log
SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
echo "######## Deposit Dashboard Process ########"
echo "Launched from: ${SCRIPT_DIR}"

########
# Redirect output to log
readonly rundate=$(date)
readonly datestamp=$(date -d"${rundate}" +%Y%m%d)
readonly LOG_FILE="${SCRIPT_DIR}/../../logs/dashboard-${datestamp}.log"
set -o errexit
echo -n | tee ${LOG_FILE}
exec 1>${LOG_FILE}
exec 2>&1
date -d"${rundate}"

echo "Launched from: ${SCRIPT_DIR}"

DASHBOARD_PACKAGE=`ls ${SCRIPT_DIR}/../libs/dashboard-*.war`
echo "Using dashboard package: ${DASHBOARD_PACKAGE}"

# EXECUTABLE="/exlibris/product/jdk-15.0.2/bin/java"
EXECUTABLE="/exlibris/product/jdk-17.0.3/bin/java"

if [ ! -x "${EXECUTABLE}" ]; then
  JAVA_HOME=`echo ${JAVA_HOME}`
  EXECUTABLE="${JAVA_HOME}/bin/java"
fi
echo "Using the java command: ${EXECUTABLE}"

exec "${EXECUTABLE}" -jar ${DASHBOARD_PACKAGE} --spring.config.location=file:${SCRIPT_DIR}/../conf/application.properties

echo "Launched the deposit dashboard at the backend."
