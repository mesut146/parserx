include "../common.g"

A: B b | C c;
B: C d | e;
C: A a | f;

/*
C: ((C d | e) b | C c) a | f;
C: C d b a | e b a | C c a | f;
*/