include "../common.g"

%start: S;

//not LALR(1) but LR(1)

S: a E a | b E b | a F b | b F a;
E: e;
F: e;

/*
S: a e a | b e b | a e b | b e a
*/