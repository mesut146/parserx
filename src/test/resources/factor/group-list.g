include "../common.g"

A: (a b)* c | (a d)? e;

/*
A = Ag1* c | Ag2? e;
Ag1 = a b;
Ag2 = a d;

A: a (Ag1(a) Ag1* c | Ag2(a) e) | c | e;
Ag1(a): a(a) b;
Ag2(a): a(a) d;
*/