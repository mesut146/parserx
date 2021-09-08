include "../common.g"

A: A a | A b | c;
/*
A: c (a | b)*;


A = A (A(A) a | A(A) b) | c;
A = c (A(c) (A(A) a | A(A) b) | c(c));


A(c) = A(c) (A(A) a | A(A) b) | c(c);
A(c): c(c) (A(A) a | A(A) b)*
*/
