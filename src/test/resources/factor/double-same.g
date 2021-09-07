include "../common.g"

A: a a b | a a c;
B: a b c | a b d;


/*

A: a (a[a] a b | a[a] a c);
A: a (a (a[a] a[a] b | a[a] a[a] c));
A: a a (a[a] a[a] b | a[a] a[a] c);

*/