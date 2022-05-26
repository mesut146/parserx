include "../common.g"

//E: ((a | c)* | (b | d)*)* x | ((a | c)* | (b | d)*)* y
E: A* x | B* y;
A: C* | D*;
C: a | c;
D: b | d;
B: K* | M*;
K: a | c;
M: b | d;