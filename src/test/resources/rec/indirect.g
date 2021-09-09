include "../common.g"


A: a? B c | d;
B: A b | e;

/*
A = A_no_A A(A)*;
A(A) = B(A) c;
A_no_A = a B c | B_no_A c | d;
B(A) = A(A) b;
B_no_A = e;

*/