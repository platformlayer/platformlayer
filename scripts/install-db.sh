#!/bin/bash

set -e

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../

cat ${BASE_DIR}/db/createdb.sql | su - postgres -c "psql"
cat ${BASE_DIR}/db/schema.sql | su - postgres -c "psql --db platformlayer"

