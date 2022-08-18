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

if [ -f pgql-lang/src/main/resources/pgql.spoofax-language ]
then
  echo "INFO: Using the parser that was previously built via 'bash install.sh'"
else
  echo "ERROR: Run 'bash install.sh' first."
  exit
fi

STAGE=false
if [[ "$1" =~ ^[0-9.]+$ ]]
then
  echo "Deploying to release repo"
else
  echo "Deploying to staging repo"
  STAGE=true
fi

VERSION_A="0.0.0-SNAPSHOT"
VERSION_B="$1"

for f in $FILES
do
  sed -i.bak "s/$VERSION_A/$VERSION_B/g" $f
  if $STAGE
  then sed -i.bak "s/release-local/stage-local/g" $f
  fi
done

cd graph-query-ir/; mvn deploy; cd ../
cd pgql-lang/; mvn deploy; cd ../

for f in $FILES
do
  sed -i.bak "s/$VERSION_B/$VERSION_A/g" $f
  if $STAGE
  then sed -i.bak "s/stage-local/release-local/g" $f
  fi
done
