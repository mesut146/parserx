include "../common.g"

A: a+ b | a+ c;
B: a* b | a+ c;
C: a* b | a* c;


//loop + normal
D: a* b | a* c | a d;

//double loop
E: a* b | F c | a f;
F: a* d | a* e;



