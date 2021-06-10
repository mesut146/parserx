token{
  a:"";
  b:"";
  c:"";
  e:"";
  r1:"";
  r2:"";
}

@start = E;
E: A a;
A: B b | C c;
B: D | E e | r1;
C: B b | r2;
D: A b | c;
