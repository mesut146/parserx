token{
 a: "a";
 b: "b";
 c: "c";
 d: "d";
 e: "e";
 f: "f";
}


E: A e | B+ f;
A: C+ a;
B: C | b;
C: c d;

/*
E: C+ A(C+) e | (C B(C) | B_no_C)+ f;
E: C+ A(C+) e | C B(C) C* f | B_no_C C* f;


E: C+ a e | (C | b)+ f;
C: c d;

E: C+ a e | C+ b (C | b)+ f | b (C | b)+ f;
E: C+ (a e | b (C | b)+ f) | b (C | b)+ f;
E: C+ (C+() A(C+) e | C+() B_no_C B+ f) | B_no_C B+ f;

*/

/*
E: C+ a e | (C | b)+ f;
C: c d;

E: C+ a e | C+ b (C | b)+ f | b (C | b)+ f;
E: C+ (a e | b (C | b)+ f) | b (C | b)+ f;
E: C+ (C+() A(C+) e | C+() B_no_C B+ f) | B_no_C B+ f;

*/