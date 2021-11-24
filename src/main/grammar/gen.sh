#!/bin/sh

dir=$(dirname $0)

#java -jar $dir/../../../target/*.jar -desc -lang java -package mesut.parserx.parser -out "$dir/../java/mesut/parserx/parser" $dir/parserx.g

#java -jar $dir/../../../target/*.jar -desc -lang java -package mesut.parserx.dfa.parser -out "$dir/../java/mesut/parserx/dfa/parser" $dir/nfaReader.g

java -jar $dir/../../../target/*.jar -desc -lang java -package mesut.parserx.regex.parser -out "$dir/../java/mesut/parserx/regex/parser" $dir/regex.g
