#!/bin/bash
set -e

VERSION=2016.05.17
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
        -DartifactId=graph-query-ir \
        -Dversion=$VERSION \
        -Dpackaging=jar \
        -Dfile=graph-query-ir/target/graph-query-ir-$VERSION.jar \
        -DpomFile=graph-query-ir/pom.xml \
        -Durl=$REPO \
        -DrepositoryId=$REPO_ID
