//lexer rules
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

//precedence goes from lower to higher
E: E ("+" | "-") E
 | E ("*" | "/") E
 | E "^" E
 | "(" E ")"
 | NUM;