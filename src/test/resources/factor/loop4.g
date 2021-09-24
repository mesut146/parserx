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
E = C+ (A(C+) e | (B_no_C B*) f) | A_nop_C e | (B_no_C B*) f;
A(C+) = C(C)+ a;
A_nop_C = a;
B(C) = C(C);
B_no_C = b;
*/
