#!/bin/bash
set -e

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m -XX:MaxPermSize=512m"
cd pgql-spoofax; mvn clean install; cd ../
mkdir -p pgql-lang/target
unzip -q pgql-spoofax/target/pgqllang-0.9.5.spoofax-language -d pgql-lang/target/spoofax-binaries
mvn package
