include "../common.g"

/*
double factor
A: a* b | a* c | a d;
A: a+ (b | c) | b | c | a d;
A: a (a* (b | c) | a(a) d) | b | c;
*/

A: a* b | a* c;


//B: a* b | C c;
//C: a* d | e;

/*
A: a (a[a] a* b | a[a] a* c) | b | c;
A: a A | b | c;
A: a* (b | c);
*/

/*
B: a* (a*[a*] b | C(a*) c) | b | C_no_a c;
C(a*): a*[a*] d;
C_no_a: d | e;


C C(List<Token> af1){
 C res;
 res.which = 1;
 C1 c1;
 c1.a = af1;
 c1.d = consume(d);
}

B B(){
 B res;
 switch(peek){
  case a:
   List<Token> af1;
   while(peek == a){
     af1.add(consume(a));
   }
   switch(peek){
    case b:
     res.which = 1;
     B1 b1;
     b1.a = af1;
     b1.b = consume(b);
    case c:
     res.which = 2;
     B2 b2;
     b2.C = C(af1)
     b2.c = consume(c);
   }
  case b:
   res.which = 1;
   B1 b1;
   b1.b = consume(b);
  case d:
  case e:
   res.which = 2;
   B2 b2;
   b2.C = C_no_a();
   b2.c = consume(c);
 }
}

*/
