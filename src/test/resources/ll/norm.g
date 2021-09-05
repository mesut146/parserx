include "../common.g"

A: a b | (c | d d)* f;

/*
A: a b | Ag1* e;
Ag1: c | d d;

Ag1 Ag1(){
 Ag1 res = new Ag1();
 switch(peek().type){
   case c:
   res.which = 1;
   res.c = consume(c);
   case d:
    res.which = 2;
    Ag12 ag12=res.ag12=new Ag12();
    ag12.d = consume(d);
    ag12.d2 = consume(d);
 }
 return res;
}

A A(){
  A res;
  switch(peek().type){
   case a:
    res.which = 1;
    A1 a1 = res.a1 = new A1();
    a1.a = consume(a);
    a1.b = consume(b);
   case c:
   case d:
   case f:
    res.which = 2;
    A2 a2 = res.a2 = new A2();
    while(flag){
     switch(peek().type){
      case c:
      case d:
       a2.g1.add(Ag1());
      default: flag=false;
     }
    }
    a2.f = consume(f);
  }
  return res;
}


*/