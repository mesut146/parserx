include "../common.g"

A: a* x | B y;
B: a B | b;

//B: a* b
//A: a* x | B y

//A: a+ (x | B(a+) y) | x | B_no_a y
//B(a+): a+() b
//B_no_a: b