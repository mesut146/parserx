token{
  PLUS: "+";
  a: "a";
  b: "b";
  c: "c";
  d: "d";
  e: "e";
  x: "x";
  y: "y";
}

A: (a | B c) c* a b a (a | b | %epsilon);
B: b b;
