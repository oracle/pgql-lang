#!/bin/bash
set -e

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m"

mvn clean package
cd target
java -jar pgql-spring-boot-app-1.0.0.jar
