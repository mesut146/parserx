include "../common.g"

E: a S1 | a c e y;
S1: b | c S3;
S3: e S1 | d;
//E_decide: a (c (e (y #2 | c #1 | b #1) | d #1) | b #1);
/*
S1: b | c (e S1 | d);
S1: b | c e S1 | c d;
S1: (c e)* (b | c d);
*/