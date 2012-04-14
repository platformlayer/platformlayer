#!/bin/bash

# We use Java 7
#export JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64/

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../

pushd ${BASE_DIR}
mvn clean package install -Dmaven.test.skip=true
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.def
popd

