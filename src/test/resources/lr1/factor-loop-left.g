include "../common.g"

%start: E;

E: A c | B b;
A: A a | a;
B: B a | a;

/*
A: a+;
B: a+;
E: a+ (c | b);

*/