include "../common.g"

%start: E;

E: A x | b A y;
//A: a B c;
A: a B;
B: a;