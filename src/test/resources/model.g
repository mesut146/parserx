token{
  PLUS: "+";
  a: "a";
  b: "b";
  c: "c";
  d: "d";
  e: "e";
  x: "x";
  y: "y";
}

A: a (x | B) b;
B: y? c+ d*;

/*
B(){
  switch(peek().type){
    case y:
      consume(y);
      break;
  }
  while(true){
    switch(peek().type){
      case c:
        consume(c);
      default:
        if(first)
        throw err:
    }
  }

}

*/

/*
class A{
  Token a;
  Ag1 g1;
  class Ag1{
    int which;
    Ag11 ag11;
    Ag12 ag12;
    class Ag11{
      Token b;
    }
    class Ag12{
      int which;
      Ag121 ag121;
      Ag122 ag122;
      class Ag121{
        Token c;
      }
      class Ag122{
        int which;
        Ag1221 ag1221;
        Ag1222 ag1222;
        class Ag1221{
         Token d;
        }
        class Ag1222{
         Token e;
        }
      }
    }
  }
}
*/

//seq: a "+" a;
//reg: a+ | b? | c* | c (a a | b);