#!/bin/bash
set -e

cd graph-query-ir/; mvn deploy; cd ../
cd pgql-lang/; mvn deploy; cd ../

