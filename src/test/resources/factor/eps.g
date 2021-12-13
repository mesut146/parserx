include "../common.g"

B: A | a b;
A: a? a;

/*
B: a a | a | a b

B: a (A(a) | a[a] b);
A(a): a[a] a | a[a]

*/