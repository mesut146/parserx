token{
 a:"";
 b:"";
 c:"";
 d:"";
 e:"";
}

/*E: A b | a c;
A: a d | e;*/

E: A a | B b;
A: e e;
B: A c | d;