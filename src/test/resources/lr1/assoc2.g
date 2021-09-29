token{
  ques: "?";
  plus: "+";
  colon: ":";
  N: [0-9]+;
}

%start = E;

%right ques;

E: N | E "?" E ":" E;