#!/bin/bash

set -e
export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m"

cd pgql-spoofax/; mvn clean install; cd ../

cd graph-query-ir/; mvn clean install; cd ../

cd pgql-lang/
mkdir -p src/main/resources/pgql-spoofax-binaries
cp ../pgql-spoofax/target/pgqllang-1.0.0.spoofax-language -d src/main/resources/pgql-1.0.spoofax-language
mvn clean install
