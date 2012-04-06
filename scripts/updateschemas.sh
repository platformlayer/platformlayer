#!/bin/bash

set -e

# Get the classpath for each project into target/classpath.def
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.def

# Build dependencies
mvn compile --projects xaas/core,shared/core-model,shared/codegen-annotations

# Generate the schema files
mvn generate-resources

for servicedir in $( find  services/ -maxdepth 1  -type d -name 'service-*' | sort )
#for servicedir in "services/service-cloud-lxc"
do
service=`basename ${servicedir}`
echo "Processing ${service}"

#mvn compile --projects ${servicedir}

CORE_FILES=$( find  shared/core-model/src/main/java -type f -name '*.java' | grep core | xargs grep -l "@Xml"  )
MODEL_FILES=$( find  ${servicedir}/src/main/java -type f -name '*.java' | xargs grep -l "@Xml" )
CLASSPATH=`cat ${servicedir}/target/classpath.def`
CLASSPATH=${CLASSPATH}:${servicedir}/target/classes
#shared/codegen-annotations/target/classes:xaas/core/target/classes:shared/core-model/target/classes:/home/justinsb/.m2/repository/com/google/guava/guava/10.0.1/guava-10.0.1.jar:${servicedir}/target/classes
OUTDIR=${servicedir}/target/schemas
mkdir -p ${OUTDIR}

echo "Doing schemagen..."
#mvn generate-resources
#schemagen -d ${OUTDIR} -cp ${CLASSPATH} ${CORE_FILES} ${MODEL_FILES}
done


#for service in services/*; do
#echo ${service}
#done

#schemagen shared/core-model/src/main/java/org/platformlayer/core/model/*.java services/service-cloud-lxc/src/main/java/org/openstack/service/lxc/model/*.java

# Copy the schemas from the target directories


pushd schemas
cp ../shared/core-model/target/schemas/*.xsd .

cp ../services/service-federation/target/generated-resources/schemagen/schema1.xsd federation.xsd
cp ../services/service-apt-cache/target/generated-resources/schemagen/schema1.xsd apt-cache.xsd
cp ../services/service-dns/target/generated-resources/schemagen/schema1.xsd dns.xsd
cp ../services/service-dnsresolver/target/generated-resources/schemagen/schema1.xsd dnsresolver.xsd
cp ../services/service-image-factory/target/generated-resources/schemagen/schema1.xsd image-factory.xsd
cp ../services/service-instance-supervisor/target/generated-resources/schemagen/schema1.xsd instance-supervisor.xsd
cp ../services/service-collectd/target/generated-resources/schemagen/schema1.xsd collectd.xsd
cp ../services/service-networks/target/generated-resources/schemagen/schema1.xsd networks.xsd
cp ../services/service-image-store/target/generated-resources/schemagen/schema1.xsd image-store.xsd
cp ../services/service-cloud-lxc/target/generated-resources/schemagen/schema1.xsd machines-lxc.xsd
cp ../services/service-cloud-raw/target/generated-resources/schemagen/schema1.xsd machines-raw.xsd
cp ../services/service-cloud-openstack/target/generated-resources/schemagen/schema1.xsd machines-openstack.xsd

#find *.xsd ! -name "platformlayer-*.xsd" | xargs sed -i 's/schema1.xsd/platformlayer-core.xsd/g'
find *.xsd ! -name "platformlayer-*.xsd" | xargs sed -i 's/schema2.xsd/platformlayer-core.xsd/g'
#find *.xsd ! -name "platformlayer-*.xsd" | xargs sed -i 's/schemaLocation=.schema..xsd.//g'

popd

# Update the bindings to the schema files
mvn generate-resources

