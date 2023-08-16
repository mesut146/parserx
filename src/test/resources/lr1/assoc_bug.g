token
{
  PLUS: "+";
  MINUS: "-";
  NUL: "*";
  DIV: "/";
  N: [0-9]+;
}

%start: E;
E: N | E op2 E %left | E op1 E %right;

op1: "+" | "-";
op2: "*" | "/";
