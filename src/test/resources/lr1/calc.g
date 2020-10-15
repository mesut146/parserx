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
expr = add | pow | term;
add = (term | pow) ("+" | "-") expr;
mul = (unary | pow) ("*" | "/") (term | pow | unary);
pow = atom "^" unary;
term = mul | atom;
unary =  atom | "-" expr | "+" expr | par;
atom = NUMBER | par;
par = "(" expr ")";

