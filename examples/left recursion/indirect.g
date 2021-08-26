token{
  a: "a";
  b: "b";
  c: "c";
  d: "d";
}

/* rules */
A: B b | c;
B: A a | d;