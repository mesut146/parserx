include "../common.g"

E: A x | a* y;
A: a A b | c;


/*
A: a{n} c b{n}
E: a{n} (A(a{n}) x | a{n}() y) | A_no_a x | y
A(a{n}): a{n}() c b{n}
*/