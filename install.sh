#!/bin/bash

set -e

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m $MAVEN_OPTS"

# change ctree into jar
# temporary workaround until the Spoofax Gradle plugin is out
cd pgql-spoofax/
sed -i.bak "s/format: ctree/format: jar/g" metaborg.yaml
sed -i.bak "s/stratego.ctree/stratego.jar/g" editor/Main.esv
mvn clean install
sed -i.bak "s/format: jar/format: ctree/g" metaborg.yaml
sed -i.bak "s/stratego.jar/stratego.ctree/g" editor/Main.esv
cd ../

cd graph-query-ir/; mvn clean install; cd ../

cd pgql-lang/
mkdir -p src/main/resources/
rm -f src/main/resources/*.spoofax-language # remove any spoofax binaries from previous builds
cp ../pgql-spoofax/target/pgqllang-0.0.0-SNAPSHOT.spoofax-language src/main/resources/pgql.spoofax-language
mvn clean install
cd ../

cd pgql-tests/
mvn test
cd spring-boot-app/
bash run.sh
cd ../../
