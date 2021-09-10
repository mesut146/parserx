include "../common.g"

A: A a | A b | c;

/*
A: c (a | b)*;
A: A_no_A A(A)*;
A(A) = A(A) a | A(A) b;
A_no_A = c;
*/
