include "../common.g"

B: A d | a b e;
A: a b c | a d;

*/
A: a (a[a] b c | a[a] d)

B: a (A(a) d | a[a] b e)

A(a): a[a] b c | a[a] d

B: a (b (A(a,b) d | a[a] b[b] e) | A_a_no_b(a) d)

A(a,b): a[a] b[b] c
A_a_no_b(a): a[a] d

*/