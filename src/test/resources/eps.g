token{
  a="";b="";c="";d="";
}

A: a B c;
B: b?;
A1: a* b;
A2: A3 b;
A3: a*;
C: B B c;
D: (d+ A)?;
E: (a | b)? c;
