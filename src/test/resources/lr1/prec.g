token{
 NUM: [0-9]+;
 POW: "^";
 PLUS: "+";
 MINUS: "-";
 MUL: "*";
 DIV: "/";
 LP: "(";
 RP: ")";
 QUES: "?";
 COLON: ":";
}

%start: E;

E:
   E "^" E %left
 | "-" E
 | E ("*" | "/") E %left
 | E ("+" | "-") E %left
 | E "?" E ":" E %right
 | "(" E ")"
 | NUM;


