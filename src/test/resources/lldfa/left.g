include "../common.g"

E: E a | b;
A: b a* c | E d;

B: B a | B b | c;
