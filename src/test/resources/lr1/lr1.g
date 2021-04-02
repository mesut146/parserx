include "../common.g"

@start: S;

S -> a E a | b E b | a F b | b F a;
E -> e;
F -> e;
