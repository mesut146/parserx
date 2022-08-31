include "../common.g"

%start: E;

/*E: A C c | d B e;
A: a | %epsilon;
B: %epsilon;
C: x | %epsilon;*/

E: A x;
A: a | %epsilon;

