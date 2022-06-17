include "../common.g"

//last prod can be empty and start with a

E: A a;
A: c y D? d*;
D: C b | f;
C: a e;

/*
E: c y A a;
A: D? d*;

E: c y A_noe a | c y a;
A_noe: D d* | D? d+;

*/
