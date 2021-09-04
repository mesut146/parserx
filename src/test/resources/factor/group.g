include "../common.g"

A: a b | (a c | d) e;

/*
A: a b | Ag1 e;
Ag1: a c | d;

A: a (a(a) b | Ag1(a) e) | Ag1_no_a e;
Ag1(a): a(a) c;
Ag1_no_a: d;
Ag1 = a Ag1(a) | Ag1_no_a;

Ag1 Ag1_no_a(){
 Ag1 res;
 res.which = 2;
 res.d = consume(d);
 return res;
}

Ag1 Ag1(Token a){
  Ag1 res = new Ag1();
  #res.which = 1;
  #Ag11 Ag11 = res.Ag11 = new Ag11();
  Ag11.a = a;
  Ag11.c = consume(c);
  return res;
}

A A(){
 A res;
 switch(peek()){
  case a:
   Token af1 = consume(a);
   switch(peek()){
    case b:
     res.which = 1;
     A1 a1 = res.a1 = new A1();
     a1.a = af1;
     a1.b = consume(b);
    case c:
     res.which = 2;
     A2 a2 = res.a2 = new A2();
     a2.Ag1 = Ag1(af1);
     a2.e = consume(e);
   }
  case d:
   res.which = 2;
   A2 a2 = res.a2 = new A2();
   a2.Ag1 = Ag1_no_a();
   a2.e = consume(e);
  default: err;
 }
 return res;
}

*/