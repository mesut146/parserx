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

@start = expr ;
expr = add | term;
add = term ("+" | "-") expr;
mul = term ("*" | "/") term;
term = mul | atom;
atom = NUMBER | "(" expr ")" | "-" expr;

