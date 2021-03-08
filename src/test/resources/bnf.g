token{

}

A: A b | A d | c;

/*A: A b | c | B | C;
B: d | ;
C: e | f | ;
D: d;
*/

/*
A: A b | c | d? (e | f)?
A: (c | d? (e | f)?) b*
*/
