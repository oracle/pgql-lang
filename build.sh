#!/bin/bash
set -e

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m -XX:MaxPermSize=512m"
cd pgql-spoofax; mvn install; cd ../
mvn package
