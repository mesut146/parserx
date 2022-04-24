include "../common.g"

A: B b | a c;
B: a d | e;


/*
A: a (A1(a) | A2(a)) | A1_no_a
A1(a): B(a) b
A2(a): a(a) c
A1_no_a: B_no_a b
B(a): B1(a)
B1(a): a(a) d
B_no_a: e

A: a (B(a) b | a(a) c) | B_no_a b;
B(a): a(a) d;
B_no_a: e;
B: a B(a) | B_no_a;

B B(){
 B res;
 switch(peek().type){
  case a:
   Token a = consume(a);
   B(a);
  case e:
 }
}

B B(Token a){
 B res = new B();
 res.which = 1;
 B1 b1 = res.b1 = new B1();
 b1.a = a;
 b1.d = consume(d);
 return res;
}

B B_no_a(){
 B res = new B();
 res.which = 2;
 res.e = consume(e);
 return res;
}

A A(){
 A res = new A();
 switch(peek){
  case a:
   Token a = consume(a);
   switch(peek){
    case d:
     res.which = 1;
     A1 a1 = res.a1 = new A1();
     a1.B = B(a);
     a1.b = consume(b);
    case c:
     res.which = 2;
     A2 a2 = res.a2 = new A2();
     a2.a = a;
     a2.c = consume(c);
   }
  case e:
   res.which = 1;
   A1 a1 = res.a1 = new A1();
   a1.B = B_no_a();
   a1.b = consume(b);
 }
 return res;
}
*/