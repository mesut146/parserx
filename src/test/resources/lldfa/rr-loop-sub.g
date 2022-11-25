include "../common.g"

F: E | a* z | t;
//F_decide: z #2 | y #1 | x #1 | t #3 | s #1 | b #1 | a (a)* (z #2 | y #1 | x #1 | b #1)

E: A* x | B* y | s;
A: a | b;
B: a | b;
//E_decide: y #2 | x #1 | s #3 | b (b | a)* (y #2 | x #1) | a (b | a)* (y #2 | x #1)