#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${SCRIPT_DIR}/common

pushd ${BASE_DIR}/auth/server-admin
CP=`cat target/classpath.def`
CP=${CP}:target/keystone-webapp-admin-1.0-SNAPSHOT.jar

${JAVA} -cp ${CP} -Dconf=../conf.db/system.conf -Dapplication.mode=development org.openstack.keystone.server.KeystoneAdminServer

popd

