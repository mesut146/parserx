#!/bin/sh

dir=$(dirname $0)

java -jar $dir/../../../target/*.jar -desc -package mesut.parserx.parser -out "$dir/../java/mesut/parserx/parser" $dir/parserx.g
