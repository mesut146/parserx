include "../common.g"

A: a b | a* c;


/*
A: a (a[a] b | a[a] a* c) | c;
*/