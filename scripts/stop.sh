#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${SCRIPT_DIR}/common

#pkill -f keystone-webapp-user-${VERSION}.jar
fuser -k -n tcp 5001 || true

#pkill -f keystone-webapp-admin-${VERSION}.jar
fuser -k -n tcp 35358 || true

#pkill -f platformlayer-xaas-webapp-${VERSION}.jar
fuser -k -n tcp 8082 || true
