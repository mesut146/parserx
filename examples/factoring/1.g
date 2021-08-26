token{
  a: "a";
  b: "b";
  c: "c";
  d: "d";
}

A: a b | a c;

B: a b | (a c)+;

C: a b | D;
D: a c | d;