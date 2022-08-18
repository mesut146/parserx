token{
  plus: "+";
  minus: "-";
  colon: ":";
  N: [0-9]+;
}

%start = E;

E: N
 | "-" E
 | E "+" E %left;