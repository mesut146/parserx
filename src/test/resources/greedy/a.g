include "../common.g"

//last prod can be empty and start with a


E: A a;
A: c y D? d*;
D: C b | f;
C: a e;

/*
E: c y ((a e) b | f)? d* a;
c y (a e b d* a | f d* a | d* a)
cya , cyaeba

E: c (y a e b d* a | y f? d* a);
*/