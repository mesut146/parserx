include "../common.g"

E: A x | X* y;
X: a | c;
A: a A b | c A d | e;

//acedbx

/*
E: y | e x | a S4 | c S3
S3: c S3 | a S4 | y | e d S8
S4: c S3 | a S4 | y | e b S9
S8: d S8 | x | b S9
S9: d S8 | x | b S9

S3: c* (a S4 | y | e d S8)
S4: a* (c S3 | y | e b S9)
S8: d S8 | x | b S8
S8: (b | d)* x

E: y | e x | a S4 | c S3
S3: c* (a S4 | y | e d (b | d)* x)
S4: a* (c S3 | y | e b (b | d)* x)

S3: c* (a a* (c S3 | y | e b (b | d)* x) | y | e d (b | d)* x)
*/