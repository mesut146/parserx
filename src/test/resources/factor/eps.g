include "../common.g"

A: a? a;
B: A | a b;
//C: a? b?;

/*
B: a a | a | a b

B: a (A(a) | a[a] b);
A(a): a[a] a | a[a]

*/