tokens{
  PLUS = "+"
  MINUS = "-"
  STAR = "*"
  DIV = "/"
  POW = "^"
  LPAREN = "("
  RPAREN = ")"
  #DIGIT=[0-9]
  NUMBER = {DIGIT}+ ("." {DIGIT}+)?
}

@start = expr ;

expr = mul (("+" | "-") expr)* ;
mul = atom (("*" | "/") mul)*;
atom = {NUMBER} | "(" expr ")" | "-" expr;

//expr = mul expr_0*;
//expr_0* = (("+" | "-") expr);

