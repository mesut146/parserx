#!/bin/sh

mvn clean
sh ./src/main/grammar/gen.sh
mvn -DskipTests=true package