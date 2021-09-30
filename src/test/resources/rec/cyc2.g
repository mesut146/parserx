include "../common.g"

%start = E;

E: A a;
A: B b | C c;
B: D | E e | x;
C: B b | y;
D: A b | c;
