#!/bin/bash

#Checking the current log level:
echo "Before, the log level is:"
curl -X "GET" "http://localhost:1901/deposit-dashboard/actuator/loggers/nz.govt.natlib.dashboard"
echo ""

#Configure the log level
echo "Going to reset log level"
curl -X "POST" "http://localhost:1901/deposit-dashboard/actuator/loggers/nz.govt.natlib.dashboard" \
     -H "Content-Type: application/json; charset=utf-8" \
     -d $'{  "configuredLevel": "ERROR"}'
echo ""

#Checking the current log level:
echo "Now, the log level is:"
curl -X "GET" "http://localhost:1901/deposit-dashboard/actuator/loggers/nz.govt.natlib.dashboard"
