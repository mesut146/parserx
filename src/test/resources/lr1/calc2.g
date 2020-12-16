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

@start = expr;
expr = expr ("+" | "-") expr;
expr = expr ("*" | "/") expr;
expr = expr "^" expr;
expr = "(" expr ")" | "-" expr";
expr = NUMBER;


