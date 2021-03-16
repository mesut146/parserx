token{
  x:"";
  a:"";
  b:"";
}

E: A | B;
B: a D | C b;
C: D x;
D: E+;

A: a;
B: b;