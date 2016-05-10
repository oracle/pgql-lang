#!/bin/bash

mvn clean package exec:java -Dexec.mainClass="oracle.pgql.lang.example.Main" -Dexec.cleanupDaemonThreads=false
