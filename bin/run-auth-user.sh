#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${SCRIPT_DIR}/common

pushd ${BASE_DIR}/auth/server-user
CP=`cat ${BASE_DIR}/auth/server-user/target/classpath.def`
CP=${CP}:${BASE_DIR}/auth/server-user/target/keystone-webapp-user-1.0-SNAPSHOT.jar

${JAVA} -cp ${CP} -Dconf=../conf.db/user.conf org.openstack.keystone.server.KeystoneUserServer

popd

