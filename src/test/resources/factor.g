token{
 a:"";
 b:"";
 c:"";
 d:"";
 e:"";
}

//E: A a c;
//A: b? | d*;
//A:b | € | D+;
//E: b a c | a c; //E0: b a c

//E: A a b | a c | d;
//A: a d | e;
//E: a b | a c | d;
E: a b | a;