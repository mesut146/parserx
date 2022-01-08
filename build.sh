#!/bin/sh

sh ./src/main/grammar/gen.sh
mvn clean
mvn -DskipTests=true package