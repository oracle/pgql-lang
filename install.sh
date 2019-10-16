#!/bin/bash

set -e

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m $MAVEN_OPTS"

cd pgql-spoofax/; mvn clean install; cd ../

cd graph-query-ir/; mvn clean install; cd ../

cd pgql-lang/
mkdir -p src/main/resources/
cp ../pgql-spoofax/target/pgqllang-1.3.0-SNAPSHOT.spoofax-language src/main/resources/pgql.spoofax-language
mvn clean install
cd ../

cd pgql-tests/
mvn test
cd spring-boot-app/
bash run.sh
cd ../../
