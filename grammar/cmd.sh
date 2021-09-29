#!/bin/sh

dir=$(dirname $0)
cc="${dir}/javacc-7.0.5.jar"
grammar="${dir}/parserx.jj"
out="${dir}/../src/main/java/mesut/parserx/grammar"
#java -cp $cc javacc -OUTPUT_DIRECTORY=${out} -FORCE_LA_CHECK=true $grammar

java -jar $dir/../target/*.jar -desc -package mesut.parserx.parser2 -out "/media/mesut/SSD-DATA/IdeaProjects/parserx/src/main/java/mesut/parserx/parser2" "/media/mesut/SSD-DATA/IdeaProjects/parserx/examples/parserx.g"
