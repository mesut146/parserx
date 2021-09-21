token
{
  PLUS = "+";
  MINUS = "-";
  STAR = "*";
  DIV = "/";
  POW = "^";
  LP = "(";
  RP = ")";
  //N = [0-9]+ ("." [0-9]+)?;
  N: [0-9]+;
}

%start = E;
%left STAR PLUS POW;

//E: E "*" E | E "+" E | N;
E: E "^" E | E "*" E | E "+" E | "(" E ")" | N;

