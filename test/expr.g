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


expr = mul (("+" | "-") expr)* ;
mul = atom (("*" | "/") mul)*;
atom = {NUMBER} | "(" expr ")" | "-" expr;
