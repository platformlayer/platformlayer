#!/bin/bash

mkdir -p ~/.credentials/

TENANT=$1
USER=$2
SECRET=$3

cat > ~/.credentials/${TENANT} <<EOF
platformlayer.tenant=${TENANT}
platformlayer.username=${USER}
platformlayer.password=${SECRET}
platformlayer.auth=http://127.0.0.1:5001/v2.0/

#auth.user.module=org.platformlayer.auth.keystone.KeystoneOpsUserModule
auth.system.module=org.platformlayer.auth.keystone.KeystoneOpsSystemModule

auth.jdbc.driverClassName=org.postgresql.Driver
auth.jdbc.url=jdbc:postgresql://127.0.0.1:5432/platformlayer
auth.jdbc.username=platformlayer_ops
auth.jdbc.password=platformlayer-password

EOF

echo "Created ~/.credentials/${TENANT}"
