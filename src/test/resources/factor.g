token{
 a:"";
 b:"";
 c:"";
 d:"";
 e:"";
}

E: A a c;
A: b?;
//E: b a c | a c; //E0: b a c

//E: A a b | a c | d;
//A: a d | e?;

/*E: (a d | e?) a b | a c | d;
E: a d a b | e? a b | a c | d;
E: a d a b | e a b | a b | a c | d;
E: a (d a b | b | c) | e a b | d;*/