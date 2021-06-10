token{
  a:"";
  b:"";
  c:"";
  e:"";
  r1:"";
  r2:"";
}

@start = E;
E: A a | B c c;
A: B b | C c;
B: A | E e | r1;
C: B b | r2;


/*
B: A | A a e | r1;
B: B b | C c | (B b | C c) a e | r1;
B: (C c | C c a) e | r1) (b | b a e)*;

*/