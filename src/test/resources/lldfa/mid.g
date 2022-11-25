include "../common.g"

//mid as factor
E: A x | a* y;
A: a A b | c;
//E_decide: y #2 | c #1 | a (a)* (y #2 | c #1);