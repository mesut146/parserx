include "../common.g"

//two factor two rule
A: a b | B c | d e | f;
B: a x | d y | x;

/*
//factor a
A: a (a(a) b | B(a) c) | B_no_a c | d e | f;
B(a): a(a) x;
B_no_a: d y | x;
B: a B(a) | B_no_a;
//factor d
A: a (a(a) b | B(a) c) | d (B_no_a(d) c | d(d) e) | B_no_a_d c | f;
B_no_a(d): d(d) y;
B_no_a_no_d: x;

A A(){
  A res;
  switch(peek().type){
    case a:
      Token a = consume(a);
      switch(peek().type){
        case b:
          res.which = 1;
          A1 a1 = res.a1 =  new A1();
          a1.a = a;
          a1.b = consume(b);
        case x:
          res.which = 2;
          A2 a2 = res.a2 = new A2();
          a2.B = B(a);
          a2.c = consume(c);
      }
    case d:
      Token d = consume(d);
      switch(){
       case y:
         res.which = 2;
         A2 a2 = res.a2 = new A2();
         a2.B = B_no_a(d);
         a2.c = consume(c);
       case e:
         res.which = 3;
         A3 a3 = res.a3 = new A3();
         a3.d = d;
         a3.e = consume(e);
      }
    case x:
      res.which = 2;
      A2 a2 = res.a2 = new A2();
      a2.B = B_no_a_d();
      a2.c = consume(c);
    case f:
      res.which = 4;
      res.f = consume(f);
  }
  return res;
}

//diff ref two factor
A: a b | B b | d e | C c | f;
B: a x | x;
C: d x | x;


*/