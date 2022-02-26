include "../common.g"

E: A* x | B* y;
A: a b c?;
B: d? a b;

//last a b goes A(a,b): a() b() c