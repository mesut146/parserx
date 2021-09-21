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
  NUMBER = DIGIT+ ("." DIGIT+)?;
}

//productions

%start = E;

%left PLUS MINUS STAR DIV POW;

E: "-" E | "(" E ")" | NUMBER;
E: E "^" E;
E: E "*" E | E "/" E;
E: E "+" E | E "-" E;


