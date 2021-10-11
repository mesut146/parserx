include "../common.g"

%start = E;

A: B b | c;
B: A a | d;

/*
A: A a b | d b | c;
A: (d b | c) (a b)*;
B: (d b | c) (a b)* a | d;

B: B b | c a | d;
B: (c a | d) b*;
A: (c a | d) b* b | c;
*/

/*
//factor c,d
A = c (B(c) b | c(c)) | B_no_c b;
B = d (A(d) a | d(d)) | A_no_d a;
A(c) = B(c) b | c[c];
B(c) = A(c) a;
B_no_c = d;
B_no_c(d) = d[d];
A(d) = B_no_c(d) b;
A_no_d = c (B(c) b | c[c]);

A(c): A(c) a b | c[c];
B(c): B(c) b a | c[c] a;

A(c): A_c_no_A_c(c) A_c(c,A(c))*;
A(c): c[c] (a b)*
A_c(c,A_c(c)): B(c,A_c(c)) b;
A_c_no_A_c(c): B_c_no_A_c(c) b | c[c];
B(c,A_c(c)): A(c)[A(c)] a;
B_c_no_A_c(c): A(c)[A(c)] a;

B_no_c: B_no_c(B_no_c) B_no_c_no_B_no_c(B_no_c)*;
B_no_c(B_no_c): A_no_c(B_no_c) a;
B_no_c_no_B_no_c: d;
A_no_c(B_no_c): B_no_c[B_no_c] b;


*/

/*
A = A_no_A A(A)*;
B = d (A(d) a | d(d)) | A_no_d a;
B(A) = A(A) a;
B_no_A = d;
A(A) = B(A) b;
A_no_A = B_no_A b | c;
B_no_A(d) = d(d);
A_no_A(d) = B_no_A(d) b;
A_no_A_no_d = c;
A(d) = A_no_A(d) A(A)*;
A_no_d = A_no_A_no_d A(A)*;
*/
/*
B = B_no_B B(B)*;
A = c (B(c) b | c(c)) | B_no_c b;
A(B) = B(B) b;
A_no_B = c;
B(B) = A(B) a;
B_no_B = A_no_B a | d;
A_no_B(c) = c(c);
B_no_B(c) = A_no_B(c) a;
B_no_B_no_c = d;
B(c) = B_no_B(c) B(B)*;
B_no_c = B_no_B_no_c B(B)*;
*/