include "../common.g"

A: a x | a y | b x | b y;

/*
A: a (a(a) x | a(a) y) | b x | b y;

A: a (a(a) x | a(a) y) | b (b(b) x | b(b) y);


A A(){
 A res;
 switch(peek().type){
  case a:
   Token a = consume(a);
   switch(peek){
    case x:
     res.which = 1;
     A1 a1 = res.a1 = new A1();
     a1.a = a;
     a1.x = consume(x);
    case y:
     res.which = 2;
     A2 a2 = res.a2 = new A2();
     a2.a = a;
     a2.y = consume(y);
   }
  case b:
 }
 return res;
}

*/