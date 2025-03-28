#!/bin/bash

set -e

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m $MAVEN_OPTS"

# change ctree into jar
# temporary workaround until the Spoofax Gradle plugin is out
cd pgql-spoofax/
sed -i.bak "s/format: ctree/format: jar/g" metaborg.yaml
sed -i.bak "s/  provider \: target\/metaborg\/stratego.ctree/\/\/  provider \: target\/metaborg\/stratego.ctree/g" editor/Main.esv
mvn clean install
sed -i.bak "s/format: jar/format: ctree/g" metaborg.yaml
sed -i.bak "s/\/\/  provider \: target\/metaborg\/stratego.ctree/  provider \: target\/metaborg\/stratego.ctree/g" editor/Main.esv
cd ../

cd graph-query-ir/; mvn clean install; cd ../

cd pgql-lang/
mkdir -p src/main/resources/
# copy parse table
cp ../pgql-spoofax/target/metaborg/sdf.tbl src/main/resources/sdf.tbl
# copy and install transformations
cp ../pgql-spoofax/target/metaborg/stratego.jar pgql-lang-trans.jar
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=pgql-lang-trans.jar -DgroupId=oracle.pg -DartifactId=pgql-lang-trans -Dversion=0.0.0-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath="$(pwd)/repo/"
rm -rf ~/.m2/repository/oracle/pg/pgql-lang-trans/
mvn clean install
cd ../

cd pgql-tests/
mvn test
cd spring-boot-app/
bash run.sh
cd ../../
