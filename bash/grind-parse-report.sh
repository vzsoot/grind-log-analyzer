#!/bin/bash

GRIND_DB_URL=${GRIND_DB_URL:=jdbc:postgresql://localhost:5432/grind-logs}
GRIND_DB_USER=${GRIND_DB_USER:=postgres}
GRIND_DB_PASS=${GRIND_DB_PASS:=123}

parent_path=$(dirname -- "$(readlink -f -- "$BASH_SOURCE")")

java -jar $parent_path/../release/grind-log-analyzer.jar --dburl=$GRIND_DB_URL --dbuser=$GRIND_DB_USER --dbpass=$GRIND_DB_PASS --report=$1
