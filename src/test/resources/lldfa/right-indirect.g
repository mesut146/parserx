include "../common.g"

A: a B | b;
B: c A | d;
//A: a c A | a d | b = (a c)* (a d | b)
E: F* g | A x;
F: a c;

//C: a D | b C | c;
//D: d C | e D | f;