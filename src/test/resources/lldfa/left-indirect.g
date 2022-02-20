include "../common.g"

A: B a | b;
B: A c | d;
//A: A c a | d a | b = (d a | b) (c a)*
//B: B a c | b c | d = (b c | d) (a c)*

C: D a | C b | c;
D: C d | D e | f;