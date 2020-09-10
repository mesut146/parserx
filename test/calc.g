token
{
  PLUS = "+";
  MINUS = "-";
  STAR = "*";
  DIV = "/";
  POW = "^";
  LPAREN = "(";
  RPAREN = ")";
  #DIGIT = [0-9];
  NUMBER = {DIGIT}+ ("." {DIGIT}+)?;
}
//productions

@start = expr ;
expr = add | term | pow;
add = (term | pow) ("+" | "-") expr;
mul = (unary | number | pow) ("*" | "/") (term2 | pow);
term2 = mul | ;
pow = atom "^" atom;
term = mul | atom;
unary =  atom | "-" expr | "+" expr;

atom = NUMBER | par;
par = "(" expr ")";

