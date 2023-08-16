token{
 PLUS: "+";
 MINUS: "-";
 MUL: "*";
 DIV: "/";
 POW: "^";
 LP: "(";
 RP: ")";
 NUM: [0-9]+;
}

%start: E;
//precedence goes from higher to lower
E: E "^" E
 | "-" E
 | E ("*" | "/") E
 | E ("+" | "-") E
 | "(" E ")"
 | NUM;
