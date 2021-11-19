token{
  ques: "?";
  plus: "+";
  colon: ":";
  N: [0-9]+;
}

%start = E;

%right ques;
//todo

E: N | E "?" E ":" E %right;