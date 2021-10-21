include "../common.g"

B: A a;
A: B b | c;

/*
B: {B b | c} a;
A: {A a} b | c;

B: B_no_B B1(B)*;
B2(c): B3(c) B1(B)*;
B_no_B: A_no_B a;
B3(c): A2(c) a;
B1(B): A1(B) a;
A: c (B2(c) b | c(c));
A_no_B: c;
A2(c): c(c);
A1(B): B(B) b;

*/