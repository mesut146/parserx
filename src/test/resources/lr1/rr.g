token{
 a:"";
 b:"";
 c:"";
 d:"";
 e:"";
}


%start -> S;
S -> a B c | b C c | a C d | b B d;
B -> e;
C -> e;