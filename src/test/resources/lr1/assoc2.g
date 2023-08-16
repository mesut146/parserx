token{
  ques: "?";
  plus: "+";
  colon: ":";
  N: [0-9]+;
  x: "x";
}

%start: E;


E: N | E "?" E ":" E %left | "x" F "x";

F: N | F "?" F ":" F %right;

/*
left means right can not be same node
E: N | E "?" E ":" N;

right means left can not be same node
F: N | N "?" F ":" F;
*/
