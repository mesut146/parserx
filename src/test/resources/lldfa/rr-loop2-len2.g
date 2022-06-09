include "../common.g"

//E: A+ B+ x | C+ D+ y;
E: A A* B B* x | C C* D D* y;
A: a b | c d;
C: a b | c d;
B: e f | g h;
D: e f | g h;
