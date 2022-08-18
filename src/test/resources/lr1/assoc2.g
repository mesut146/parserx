token{
  ques: "?";
  plus: "+";
  colon: ":";
  N: [0-9]+;
}

%start: E;


E: N | E "?" E ":" E %right;