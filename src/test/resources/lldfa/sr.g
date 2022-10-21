include "../common.g"

E: A C c d x | a B c d y;
A: a;
B: b;
C: b | e;

//E -> a (e#1 | b c d (x#1 | y#2))
