#!/bin/bash

set -e

MY_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${MY_DIR}/
SCRIPT_DIR=${BASE_DIR}/bin

${SCRIPT_DIR}/install-prereqs.sh
${SCRIPT_DIR}/install-build.sh

sudo ${SCRIPT_DIR}/install-db.sh



