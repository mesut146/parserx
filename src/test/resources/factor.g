token{
 a:"";
 b:"";
 c:"";
 d:"";
 e:"";
 m:"m";
 n:"n";
 p:"p";
 x:"x";
 y:"y";

}

//enhanced
/*
A: B c | a b d | x y;
B: a b | a p | m n;
*/

//regex
/*A: a* b | B c | d;
B: x? a* m;*/

B: x C m | C m;
C: a*;

B: C m | x C m | m

/*
A: a* (a*(a*) b | B(a*) c) | d | B_no_a* c;
B(a*): x? a*(a*) m;
B_no_a*: x m | m = x? m;
*/

/*
E: A b | a c;
A: a d | e;
*/


/*E: A a | B b;
A: e e;
B: A c | d;*/


/*E: A | B;
A: a b | c;
B: E c | d;*/

/*
E(A): € | B1;
B1: E(A) c;
B0: E0 c | d;
E0: B0;

E(A): € | E(A) c | B0;
E(A): B0? c*;
E0: E0 c | d;
E0: d c*;


*/