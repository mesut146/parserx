token{
  PLUS = "+"
  MINUS = "-"
  STAR = "*"
  DIV = "/"
  POW = "^"
  LPAREN = "("
  RPAREN = ")"
  #DIGIT = [0-9]
  NUMBER = {DIGIT}+ ("." {DIGIT}+)?
}

@start = expr ;

expr = mul (("+" | "-") expr)*;
mul = atom (("*" | "/") mul)*;
atom = NUMBER | "(" expr ")" | "-" expr;

/*
expr = mul expr_0*;
expr_0* = ;
expr_0* = expr_0*_g expr;
expr_0*_g = "+";
expr_0*_g =  "-";
mul = atom mul_0*;
mul_0* = ;
mul_0* = mul_0*_g mul;
mul_0*_g = "*";
mul_0*_g = "/";
atom = {NUMBER};
atom = "(" expr ")";
atom = "-" expr;
*/

