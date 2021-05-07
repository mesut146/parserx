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
B: A | E e | r1;
C: B b | r2;

/*
//normalize A
A: (A | E e | r1) b | C c; sub B
A: (E e | r1) b | C c b*;

E: ((E e | r1) b | C c b*) a;
E: ((r1 b | C c b*) a) (e b a)*;
E: ((r1 b | ((A | E e | r1) b | r2) c b*) a) (e b a)*

*/