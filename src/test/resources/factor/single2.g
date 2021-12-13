include "../common.g"

A: a C;
C: B b | a c;
B: a d | e;


/*
C: a (B(a) b | a(a) c) | B_no_a b;
B(a): a(a) d;
B_no_a: e;

*/