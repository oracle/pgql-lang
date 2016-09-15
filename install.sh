#!/bin/bash

set -e
export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m"

cd pgql-spoofax/; mvn clean package; cd ../

cd graph-query-ir/; mvn clean install; cd ../

cd pgql-lang/
mkdir -p src/main/resources/
cp ../pgql-spoofax/target/pgql-lang-1.0.3-SNAPSHOT.spoofax-language src/main/resources/pgql-1.0.spoofax-language
mvn clean install
cd ../
