include "../common.g"

%start = E;

E: A* c | a* d;
A: a | b;