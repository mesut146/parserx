include "../common.g"

%start: E;

E: A B x | c C y;
A: a | %epsilon;
B: b | %epsilon;
C: %epsilon;

