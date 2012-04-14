#!/bin/bash

set -e

function run_as_postgres() {
	case $OSTYPE in
		darwin*) $1 ;;
		*) su - postgres -c "$1";;
	esac
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../

cat ${BASE_DIR}/db/createdb.sql | run_as_postgres "psql --host 127.0.0.1 --db template1"
cat ${BASE_DIR}/db/schema.sql | run_as_postgres "psql --host 127.0.0.1 --db platformlayer"

