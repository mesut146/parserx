include "../common.g"

A: B b | a;
B: c;

/*
Ast.A A(){
  Ast.A res = new Ast.A();
  switch(la.type){
      case c:
      {
          A1 a1 = res.a1 = new A1();

          B a1 = a1.B = new Ast.B();
          B B = new Ast.B();
          a1.B = B;
          res.c = consume(Tokens.c);
          a1.b = consume(Tokens.b);
          break;
      }
      case a:
      {
          res.which = 2;
          res.a = consume(Tokens.a);
          break;
      }
      default:{
          throw new RuntimeException("expecting one of [c,a] got: "+la);
      }
  }
  return res;
}

*/