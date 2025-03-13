#!/bin/sh

cp ../pgql-spoofax/target/metaborg/stratego.jar pgql-lang-trans.jar
mvn org.apache.maven.plugins:maven-install-plugin:2.3.1:install-file -Dfile=pgql-lang-trans.jar -DgroupId=oracle.pg -DartifactId=pgql-lang-trans -Dversion=0.0.0-SNAPSHOT -Dpackaging=jar -DlocalRepositoryPath="$(pwd)/repo/"
