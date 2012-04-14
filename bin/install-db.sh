#!/bin/bash

set -e

function run_as_postgres() {
	DB="$1"

	case $OSTYPE in
		darwin*) psql --host 127.0.0.1 --db ${DB} ;;
		*) su - postgres -c "psql --db ${DB}";;
	esac
}

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
BASE_DIR=${SCRIPT_DIR}/../

cat ${BASE_DIR}/db/createdb.sql | run_as_postgres template1
cat ${BASE_DIR}/db/schema.sql | run_as_postgres platformlayer

