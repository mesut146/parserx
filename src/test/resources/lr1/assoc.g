token
{
  PLUS: "+";
  MINUS: "-";
  STAR: "*";
  DIV: "/";
  POW: "^";
  LP: "(";
  RP: ")";
  N: [0-9]+;
}

%start: E;
E: N
 | E "+" E %left
 | E "*" E %right;




