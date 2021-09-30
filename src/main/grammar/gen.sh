#!/bin/sh

dir=$(dirname $0)

java -jar $dir/../../../target/*.jar -desc -package mesut.parserx.parser -out "../java/mesut/parserx/parser" "parserx.g"
