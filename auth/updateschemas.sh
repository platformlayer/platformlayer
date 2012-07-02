#!/bin/bash

set -e

# Generate the schema files
mvn generate-resources

# Copy the generated schema(s)
cp server-shared/target/generated-resources/schemagen/schema1.xsd client/src/main/schemas/keystone.xsd 

# Update the bindings to the schema files
mvn generate-resources

