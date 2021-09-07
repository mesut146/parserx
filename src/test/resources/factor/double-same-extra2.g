include "../common.g"

B: a a d | a x | e;
A: a a b | B b | c;

/*
//factor a
B: a{f1} (a[a] a d | a[a] x) | e;
//factor a
A: a (a[a] a b | B(a) b) | B_no_a b | c;
B(a): a[a] (a[a] a d | a[a] x)//spec factor & factored
B_no_a: e;
//factor a
A: a (a (a[a] a[a] b | B(a,a) b) | B_a_no_a(a) b) | B_no_a b | c;
B(a,a): a[a] a[a] a[a] d;
B_a_no_a(a): a[a] a[a] x;
*/