#!/usr/bin/bash

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
echo "Launched from: ${SCRIPT_DIR}"

DASHBOARD_PACKAGE=`ls ${SCRIPT_DIR}/../libs/dashboard-*.war`
echo "Using dashboard package: ${DASHBOARD_PACKAGE}"

RUNNING_LINE=`ps -ef|grep ${DASHBOARD_PACKAGE}`
echo "Will stop the process: ${RUNNING_LINE}"
PID=`echo ${RUNNING_LINE} | awk '{print $2}'`
echo "The PID of the deposit dashboard: ${PID}"

kill ${PID}

echo "Stopped!"