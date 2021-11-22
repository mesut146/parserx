include "../common.g"

A: a a b | B b | c;
B: a a d | a x | e;

/*
A: a (a(a) a b | B(a) b) | B_no_a b | c;
B(a): a[a] a d | a[a] x;
B_no_a: e;

A: a (a (a(a) a(a) b | B(a,a) b) | B_a_no_a(a) b) | B_no_a b | c;
B(a,a): a(a) a(a) d;
B_a_no_a(a): a(a) x;
*/