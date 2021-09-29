include "../common.g"

%start: E;

E: A c | B b;
A: a A | a;
B: a B | a;

/*
E: a+ (c | b);
*/