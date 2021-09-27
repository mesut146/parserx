include "common.g"

//A: (a | b)* (c d)+ e*;

A: (a? b)* c;

/*
A: A1 A2 A3;
A1: A1 a | A1 b | €;
A2: A2 c d | c d;

*/