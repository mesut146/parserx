include "../common.g"

A: (a b)* c | (a d)? e;

/*
A = Ag1* c | Ag2? e;
Ag1 = a b;
Ag2 = a d;

A: a (Ag1(a) Ag1* c | Ag2(a) e) | Ag1_no_a Ag1* c | c | Ag2_no_a e | e;
Ag1(a): a(a) b;
Ag1_no_a: null;
Ag2(a): a(a) d;
Ag2_no_a: null;
Ag1: a Ag1(a);
Ag2: a Ag2(a);
A: a (Ag1(a) Ag1* c | Ag2(a) e) | c | e;

A A(){
 A res;
 switch(peek().type){
  case a:
   Token a = consume(a);
   switch(peek().type){
    case b:
     res.which = 1;
     A1 a1 = res.a1 = new A1();
     a1.g1.add(Ag1(a));
     while(flag){

     }
     a1.c = consume(c);
    case d:
     res.which = 2;
     A2 a2 = res.a2 = new A2();
     a2.g.add(Ag2(a));
     a2.e = consume(e);
   }
  case c:
   res.which = 1;
   A1 a1 = res.a1 = new A1();
  case e:
   res.which = 2;
   A2 a2 = res.a2 = new A2();
 }
 return res;
}

*/