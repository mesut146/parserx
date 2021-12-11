include "../common.g"

B: A c | d | B e;
A: B a | b;

/*
A: A_no_A A(A)*;
A_no_A: B_no_A a | b;
A(A): B(A) a;
B_no_A: d | B_no_A e;
B(A): A(A) c | B(A) e;
*/

/*
d,de,bc,  dac,deac
B: B_no_B B(B)*;
B: (b c | d) (a c | B(B) e)*
B(B): A(B) c | B(B) e;
B_no_B: A_no_B c | d;
A(B): B(B) a;
A_no_B: b;

*/
