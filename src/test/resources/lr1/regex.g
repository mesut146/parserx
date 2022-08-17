include "../common.g"

%start: E;

//E: a* x;
//E: a x;

E: a? A? x;
A: b? c d? | c e;