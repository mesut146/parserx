include "../common.g"

A: a* b | a+ c;

/*
A: a (a[a] a* b | a[a] a* c) | b;
A: a (a* (a[a] a*[a*] b | a[a] a*[a*] c) | b | c) | b;

a* b = a+ b | b

*/

/*
A: a* B c;
B: a | b;
B(a): a[a]
B_no_a: b;

A: a A | B c;
A: a (a[a] A | B(a) c) | B_no_a c;


(a a) B=(a c) -> a A=(a A=(B=a c))



(a a a) B=(b c) -> a A=(a A=(a A=(a B=b c)))

A A(){
 A res;
 switch(peek){
  case a:
   Token af1=consume(a);
   switch(){
     case a:
     case b:
      A tmp = A();
      if(tmp.a == null){

      }
      else{

      }
     default:
      res.which = 2;
      res.B = B(af1);
      res.c = consume(c);
   }
  case b:
   res.which = 2;
   res.B = B_no_a();
   res.c = consume(c);
 }

}
*/