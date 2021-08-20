token{
 a:"";
 b:"";
 c:"";
 d:"";
 e:"";
}

/*
E: A b | a c;
A: a d | e;
*/


E: A a | B b;
A: e e;
B: A c | d;


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