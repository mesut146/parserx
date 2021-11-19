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

%start: E;

E: E "+" E %left | N;
//E2: E "+" E | N;
//E: E2;

/*
if left
E: E "+" N | N;
E: N ("+" N)*;
if right
E: N "+" E | N;
E: (N "+")* N;
none
E: N ("+" E)*;//right
*/

