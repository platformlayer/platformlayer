#!/bin/bash -x

set -e

# Get the classpath for each project into target/classpath.def
mvn dependency:build-classpath -Dmdep.outputFile=target/classpath.def

# Build dependencies
mvn compile --projects xaas/core,shared/core-model,shared/codegen-annotations

# Generate the schema files
mvn generate-resources --projects xaas/core,shared/core-model,shared/codegen-annotations


# Copy the schemas from the target directories
pushd schemas
cp ../shared/core-model/target/generated-resources/schemagen/schema1.xsd platformlayer-instancesupervisor.xsd
cp ../shared/core-model/target/generated-resources/schemagen/schema2.xsd platformlayer-imagefactory.xsd
cp ../shared/core-model/target/generated-resources/schemagen/schema3.xsd platformlayer-dns.xsd
cp ../shared/core-model/target/generated-resources/schemagen/schema4.xsd platformlayer-metrics.xsd
cp ../shared/core-model/target/generated-resources/schemagen/schema5.xsd platformlayer-jobs.xsd
cp ../shared/core-model/target/generated-resources/schemagen/schema6.xsd platformlayer-core.xsd

find *.xsd -name "platformlayer-*.xsd" | xargs sed -i 's/schema1.xsd/platformlayer-instancesupervisor.xsd/g'
find *.xsd -name "platformlayer-*.xsd" | xargs sed -i 's/schema2.xsd/platformlayer-imagefactory.xsd/g'
find *.xsd -name "platformlayer-*.xsd" | xargs sed -i 's/schema3.xsd/platformlayer-dns.xsd/g'
find *.xsd -name "platformlayer-*.xsd" | xargs sed -i 's/schema4.xsd/platformlayer-metrics.xsd/g'
find *.xsd -name "platformlayer-*.xsd" | xargs sed -i 's/schema5.xsd/platformlayer-jobs.xsd/g'
find *.xsd -name "platformlayer-*.xsd" | xargs sed -i 's/schema6.xsd/platformlayer-core.xsd/g'
popd

mvn generate-resources

pushd schemas
cp ../services/service-federation/target/generated-resources/schemagen/schema1.xsd federation.xsd
cp ../services/service-dns/target/generated-resources/schemagen/schema1.xsd dns.xsd
cp ../services/service-dnsresolver/target/generated-resources/schemagen/schema1.xsd dnsresolver.xsd
cp ../services/service-image-factory/target/generated-resources/schemagen/schema1.xsd image-factory.xsd
cp ../services/service-instance-supervisor/target/generated-resources/schemagen/schema1.xsd instance-supervisor.xsd
cp ../services/service-collectd/target/generated-resources/schemagen/schema1.xsd collectd.xsd
cp ../services/service-networks/target/generated-resources/schemagen/schema1.xsd networks.xsd
cp ../services/service-image-store/target/generated-resources/schemagen/schema1.xsd image-store.xsd
#cp ../services/service-cloud-lxc/target/generated-resources/schemagen/schema1.xsd machines-lxc.xsd
#cp ../services/service-cloud-raw/target/generated-resources/schemagen/schema1.xsd machines-raw.xsd
#cp ../services/service-cloud-openstack/target/generated-resources/schemagen/schema1.xsd machines-openstack.xsd

# TODO: Yuk... it would be good to fix this up properly
#find *.xsd ! -name "platformlayer-*.xsd" | xargs sed -i 's/schema1.xsd/platformlayer-core.xsd/g'
find *.xsd ! -name "platformlayer-*.xsd" | xargs sed -i 's/schema2.xsd/platformlayer-core.xsd/g'
find *.xsd ! -name "platformlayer-*.xsd" | xargs sed -i 's/schema3.xsd/platformlayer-core.xsd/g'
#find *.xsd ! -name "platformlayer-*.xsd" | xargs sed -i 's/schemaLocation=.schema..xsd.//g'

popd

# Update the bindings to the schema files
mvn generate-resources

