include "../common.g"

A: a a b | B b | c;
B: a a d | a x | e;

/*
A: a (a(a) a b | B(a) b) | B_no_a b | c;
B(a): a[a] a d | a[a] x;
B_no_a: e;

A: a (a (a(a) a(a) b | B(a,a) b) | B_a_no_a(a) b) | B_no_a b | c;
B(a,a): a(a) a(a) d;
B_a_no_a(a): a(a) x;

A A(){
  A res;
  switch(peek().type){
    case a:{
      Token af1 = consume(a);
      switch(peek.type){
        case a:
          Token af2 = consume(a);
          switch(peek().type){
            case b:
              res.which = 1;
              A1 a1 = res.a1 = new A1();
              a1.a = af1;
              a1.a2 = af2;
              a1.b = consume(b);
            case d:
              res.which = 2;
              A2 a2 = res.a2 = new A2();
              a2.B = B(af1,af2);
              a2.b = consume(b);
          }
        case x:
          res.which = 2;
          A2 a2 = res.a2 = new A2();
          a2.B = B_a_no_a(af1);
          a2.b = consume(b);
      }
    }
    case e:
      res.which = 2;
      A2 a2 = res.a2 = new A2();
      a2.B = B_no_a();
      a2.b = consume(b);
    case c:
      res.which = 3;
      rec.c = consume(c);
  }
  return res;
}
*/