#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${SCRIPT_DIR}/common

LOG_DIR=${BASE_DIR}/logs
mkdir -p ${LOG_DIR}

${SCRIPT_DIR}/run-auth-user.sh > ${LOG_DIR}/auth-user.log 2>&1 & 
${SCRIPT_DIR}/run-auth-system.sh > ${LOG_DIR}/auth-system.log 2>&1 &
${SCRIPT_DIR}/run-platformlayer-system.sh > ${LOG_DIR}/platformlayer.log 2>&1 &

# This shows the contents of all the logs, but it's overwhelming
# sleep 1 # Wait for logs to be created
#tail -f ${LOG_DIR}/*.log &





