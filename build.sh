#!/bin/bash
set -e

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m -XX:MaxPermSize=512m"
cd pgql-spoofax; mvn clean install; cd ../
mkdir -p pgql-lang/target
unzip -oq pgql-spoofax/target/pgqllang-1.0.0.spoofax-language -d pgql-lang/target/spoofax-binaries
mvn package
