include "../common.g"

A: a? a;
B: A | a b;

C: a? b?;

/*
B: a a | a | a b

B: a (A(a) | a[a] b);
A(a): a[a] a | a[a]


A A(){
 A res;
 if(a) res.a=consume(a);
 res.a2=consume(a);
}

A A(a){
  A res;
  switch(peek().type){
    case a:
     res.a = a;
     res.a2=consume(a);
    default:
     res.a2 = a;
  }
}

B B(){
 B res;
 Token af1 = consume(a);
 switch(peek){
  case a:
   res.which = 1;
   res.A = A(af1)
  case b:
   res.which = 2;
   B2 b2;
   b2.a = af1;
   b2.b = consume(b);
  default:
   res.which = 1;
   res.A = A(af1);
 }
}
*/