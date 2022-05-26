include "../common.g"

//E: ((a p | c) | (a b | d))* x | ((a p | c) | (a b | d))* y
E: A* x | B* y;
A: C* | D*;
C: a p | c;
D: a b | d;
B: K* | M*;
K: a p | c;
M: a b | d;



