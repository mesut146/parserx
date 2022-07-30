include "../common.g"


A: B a | C b | x;
B: A c | C d | y;
C: A e | B f | z;

/*
A: A_no_A A_A(A)*;
A_no_A: B_no_A a | C_no_A b | x;
B_no_A: C_no_A d | y;
C_no_A: B_no_A f | z;
*/

A_A(A): B_A(A) a | C_A(A) b;
B_A(A): A() c | C(A) d;
C_A(A): A() e | B(A) f;
*/