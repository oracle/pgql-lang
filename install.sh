#!/bin/bash
set -e

VERSION=2016.07.28-2
GROUP=oracle.pgx

mvn install:install-file \
        -DgroupId=oracle.pgx \
        -DartifactId=pgql-lang \
        -Dversion=$VERSION \
        -Dpackaging=jar \
        -Dfile=pgql-lang/target/pgql-lang-$VERSION.jar \
        -DpomFile=pgql-lang/pom.xml

mvn install:install-file \
        -DgroupId=oracle.pgx \
        -DartifactId=graph-query-ir \
        -Dversion=$VERSION \
        -Dpackaging=jar \
        -Dfile=graph-query-ir/target/graph-query-ir-$VERSION.jar \
        -DpomFile=graph-query-ir/pom.xml
