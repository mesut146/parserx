token{
 a: "a";
 b: "b";
 c: "c";
 d: "d";
 e: "e";
 f: "f";
}


E: A e | B* f;
A: C* a;
B: C | b;
C: c d;

/*
E: C* A(C*) e | C* B_no_C B* f | b B* f | A_nos_C e | f;

*/

/*
(C B(C) | B_no_C)* = C* B_no_C B* | B_no_C B*

*/