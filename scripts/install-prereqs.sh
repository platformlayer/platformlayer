#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../

mkdir -p ${BASE_DIR}/repos

pushd ${BASE_DIR}/repos

# We need a fixed version of jaxb2-maven-plugin
if [[ ! -e jaxb2-maven-plugin ]]; then
	git clone https://github.com/platformlayer/jaxb2-maven-plugin
else
	pushd jaxb2-maven-plugin
	git pull
	popd
fi

pushd jaxb2-maven-plugin
mvn install -Dmaven.test.skip=true
popd

# We want the openstack-java bindings
if [[ ! -e openstack-java ]]; then
	git clone https://github.com/platformlayer/openstack-java
else
	pushd openstack-java
	git pull
	popd
fi

pushd openstack-java
mvn install -Dmaven.test.skip=true
popd

# We use our Java 7 fileprovider
#if [[ ! -e openstack-fileprovider ]]; then
#	git clone https://github.com/platformlayer/openstack-fileprovider
#else
#	pushd openstack-fileprovider
#	git pull
#	popd
#fi

#pushd openstack-fileprovider
#mvn install -Dmaven.test.skip=true
#popd

popd


pushd ${BASE_DIR}/utils

# We use a helper to generate GWT boilerplate (although we're not using this at the moment)
pushd gwt-codegen
mvn install
popd

popd

