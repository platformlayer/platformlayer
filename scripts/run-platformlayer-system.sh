#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
. ${SCRIPT_DIR}/common

SERVICE_CP=""

# We could enable them all like this
for f in ${BASE_DIR}/services/service-*/target/service*.jar; do
SERVICE_CP="${SERVICE_CP}:${f}"
done

echo "Services: ${SERVICE_CP}"

pushd ${BASE_DIR}/xaas/webapp
CP=`cat target/classpath.def`
CP=${CP}:target/platformlayer-xaas-webapp-1.0-SNAPSHOT.jar
CP=${CP}:${SERVICE_CP}

${JAVA} -cp ${CP} org.platformlayer.xaas.web.PlatformLayerServer

popd

