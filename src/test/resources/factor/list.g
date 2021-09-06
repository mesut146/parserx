include "../common.g"

A: a b | a* c;

/*
A: a (a[a] b | a[a] a* c) | c;

A A(){
 A res;
 switch(peek().type){
  case a:
   Token a = consume(a);
   switch(peek){
    case b:
     res.which = 1;
     A1 a1 = res.a1 = new A1();
     a1.a = a;
     a1.b = consume(b);
    case a:
    case c:
     res.which = 2;
     A2 a2 = res.a2 = new A2();
     a2.a.add(a);
     while(peek==a){
      a2.a.add(consume(a));
     }
     a2.c = consume(c);
   }
  case c:
   res.which = 2;
   A2 a2 = res.a2 = new A2();
   a2.c = consume();
 }
 return res;
}

*/