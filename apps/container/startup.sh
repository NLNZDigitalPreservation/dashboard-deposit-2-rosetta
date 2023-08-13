#!/usr/bin/bash

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" &> /dev/null && pwd)
echo "######## Deposit Dashboard Process ########"
echo "Launched from: ${SCRIPT_DIR}"

podman run -itd --rm \
       --name deposit-dashboard \
       -p 1901:1901 \
       -v /exlibris/dps/nlnz_tools/dashboard/:/exlibris/dps/nlnz_tools/dashboard/ \
       -v ${SCRIPT_DIR}/../conf/application.properties:/deployment/conf/application.properties \
       localhost/deposit-dashboard:v2.0
