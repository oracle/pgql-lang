#!/bin/bash

export MAVEN_OPTS="-Xms512m -Xmx1024m -Xss16m $MAVEN_OPTS"

mvn clean package exec:java -Dexec.mainClass="oracle.pgql.lang.example.Main" -Dexec.cleanupDaemonThreads=false
