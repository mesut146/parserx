include "../common.g"

%start: E;

E: A A;
A: a A | b;