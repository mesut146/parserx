include "../common.g"

E: A* c | a* d;
A: a | b;
//E -> a (a)* (c | d | b) | c | d | b

//B: a* b c | a d e;
//B -> a (d | (a | b)) | b
