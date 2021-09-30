include "../common.g"

%start = E;

E: A a | B c c;
A: B b | C c;
B: A | E e | x;
C: B b | y;

/*
E(E): A(E) a | B(E) c c;
E_no_E: A_no_E a | B_no_E c c;
A(E): B(E) b | C(E) c;
A_no_E: B_no_E b | C_no_E c;
B(E): A(E) | E[E] e;
B_no_E: A_no_E | x;
C(E): B(E) b;
C_no_E: y;

*/