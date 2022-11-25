include "../common.g"

A: a* x | B y;
B: a B | b;
//A_decide: (a)* (x #1 | b #2);
