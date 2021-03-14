token{
  x:"";
  a:"";
  b:"";
}

E: A | B | C;
C: a D | D b;
D: F x;
F: E+;

A: a;
B: b;