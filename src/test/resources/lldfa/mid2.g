include "../common.g"

E: A x | X* y;
X: a | c;
A: a A b | c A d | e;

//acedbx

//E: y | e x | a S4 | c S3
//S3: c S3 | a S4 | y | e d (x | b S9 | d S10)
//S4: c S3 | a S4 | y | e b S9