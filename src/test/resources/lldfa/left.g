include "../common.g"

%start = E;

E: E a | b;
A: b a* c | E d;
