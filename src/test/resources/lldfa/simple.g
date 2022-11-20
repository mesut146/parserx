include "../common.g"

E: A B x | A D y;
A: a | b;
B: c | d;
D: c | d;

//S0: b (d (y | x) | c (y | x)) | a (d (y | x) | c (y | x))
//S0: (a | b) (c | d) (x | y)