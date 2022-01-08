include "../common.g"

E: A e | B+ f;
A: C+ a;
B: C | b;
C: c d;

/*
(C B(C) | B_no_C)+
(C B(C))+ (B_no_C B*)?

*/