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
A: a* b | a* c | a d;

//A: a+ (b | c) | b | c | a d;
//A: af2=a (a(a) af1=a* (b | c) | a(a) d) | b | c;




