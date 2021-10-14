include "../common.g"

%start = B;

A: B b | c;
B: A a | d;

/*
A: (A a | d) b | c;

*/

/*
A: A a b | d b | c;
A: (d b | c) (a b)*;
B: (d b | c) (a b)* a | d;

B: B b | c a | d;
B: (c a | d) b*;
A: (c a | d) b* b | c;
*/

/*
A: A1 A2(A)*;
A1: B1 b | c;
A2: B2 b;
A3: A5 A2(A)*;
A4: A6 A2(A)*;
A5: c;
A6: B4 b;
B: d (A4 a | d(d)) | A3 a;
B1: d;
B2: A(A) a;
B4: d(d);
*/

