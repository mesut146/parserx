token{
  ques: "?";
  plus: "+";
  colon: ":";
  N: [0-9]+;
}

%start = E;
%left ques;


E: N
 | E "?" E ":" E;