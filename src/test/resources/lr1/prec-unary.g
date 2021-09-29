token{
  plus: "+";
  minus: "-";
  colon: ":";
  N: [0-9]+;
}

%start = E;
%left plus;


E: N
 | "-" E
 | E "+" E;