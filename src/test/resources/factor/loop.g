include "../common.g"

/*
A1: a* b | a* c;
A2: a* b | a+ c;
A3: a+ b | a+ c;
*/

/*
B: a* b | C c;
C: a* d | e;
*/


//double factor
//A: a* b | a* c | a d;

//double loop
A: a* b | B c | a f;
B: a* d | a* e;


