#!/usr/bin/bash

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
echo "Launched from: ${SCRIPT_DIR}"

DASHBOARD_PACKAGE=`ls ${SCRIPT_DIR}/../libs/dashboard-*.war`
echo "Using dashboard package: ${DASHBOARD_PACKAGE}"

while :
do
RUNNING_LINE=`ps -ef|grep ${DASHBOARD_PACKAGE}|grep spring.config.location`
if [ -z "${RUNNING_LINE}" ]; then
        echo "No more dashboard found"
        break
fi
echo "Will stop the process:"
echo "${RUNNING_LINE}"

PID=`echo ${RUNNING_LINE} | awk '{print $2}'`
echo "The PID of the deposit dashboard: ${PID}"
kill ${PID}
sleep 3

if [ -d "/proc/${PID}" ]; then
        echo "Process ${PID} is still running, try to kill it forced"
        kill -9 ${PID}
else
        echo "Process ${PID} stopped!"
fi

done

