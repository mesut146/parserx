include "../common.g"

E: A* x | B* y;
A: a b | c d;
B: a b | c d;

F: X | Y;
X: A* x;
Y: B* y;
//F-> (c d)* (y | x | a b F)