#!/bin/bash

# Generate the schema files
mvn generate-resources

# Copy the generated schema(s)
cp server-shared/target/schemas/schema1.xsd client/src/main/schemas/keystone.xsd 

# Update the bindings to the schema files
mvn generate-resources

