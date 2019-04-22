#!/bin/bash
set -e

FILES="
graph-query-ir/pom.xml
pgql-lang/pom.xml
"

if [ $# -eq 0 ]
  then
    echo "ERROR: Please specify a version number, for example, 'bash deploy.sh 2017-01-01'."
    exit
fi

if [ -f pgql-lang/src/main/resources/pgql-1.2.spoofax-language ]
then
  echo "INFO: Using the parser that was previously built via 'bash install.sh'"
else
  echo "ERROR: Run 'bash install.sh' first."
  exit
fi

VERSION_A="1.2.0-SNAPSHOT"
VERSION_B="$1"

for f in $FILES
do
  sed -i "s/$VERSION_A/$VERSION_B/g" $f
done

cd graph-query-ir/; mvn deploy; cd ../
cd pgql-lang/; mvn deploy; cd ../

for f in $FILES
do
  sed -i "s/$VERSION_B/$VERSION_A/g" $f
done
