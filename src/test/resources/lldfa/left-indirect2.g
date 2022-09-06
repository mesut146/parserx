include "../common.g"

A: A a | B b | x;
B: B c | A d | y;

/*
A: (B b | x) a*
B: (A d | y) c*
A: ((A d | y) c* b | x) a*
A: (A d | y) c* b a* | x a*
A: A d c* b a* | y c* b a* | x a*
A: (y c* b a* | x a*) (d c* b a*)*

xdbdb

B2{A3{x},d}
*/