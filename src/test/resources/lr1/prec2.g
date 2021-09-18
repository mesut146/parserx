token{
  ques: "?";
  plus: "+";
  colon: ":";
  R: "rest";
}

%start = E;
E: E "+" E
 | E "?" E ":" E
 | R;