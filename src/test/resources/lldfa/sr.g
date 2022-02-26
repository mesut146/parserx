include "../common.g"

E: A C c d x | a B c d y;
A: a;
B: b;
C: b | e;

//E: a (b c d (A(a) C(b) c() d() x | a() B(b) c() d() y) | A(a) C2 c d x)