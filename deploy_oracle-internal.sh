#!/bin/bash
set -e

VERSION=2016.05.02-8
GROUP=oracle.pgx
REPO=http://artifactory-slc.oraclecorp.com/artifactory/simple/labs-pgx-release-local
REPO_ID=labs-pgx-release-local

mvn deploy:deploy-file \
        -DgroupId=oracle.pgx \
        -DartifactId=pgql-lang \
        -Dversion=$VERSION \
        -Dpackaging=jar \
        -Dfile=pgql-lang/target/pgql-lang-$VERSION.jar \
        -DpomFile=pgql-lang/pom.xml \
        -Durl=$REPO \
        -DrepositoryId=$REPO_ID

mvn deploy:deploy-file \
        -DgroupId=oracle.pgx \
        -DartifactId=querygraph-ir \
        -Dversion=$VERSION \
        -Dpackaging=jar \
        -Dfile=querygraph-ir/target/querygraph-ir-$VERSION.jar \
        -DpomFile=querygraph-ir/pom.xml \
        -Durl=$REPO \
        -DrepositoryId=$REPO_ID
