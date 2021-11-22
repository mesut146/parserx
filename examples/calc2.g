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

expr: mul (("+" | "-") mul)*;
mul: unary (("*" | "/") unary)*;
unary: "-" unary | pow;
pow: atom ("^" atom)*;
atom: "(" expr ")" | NUM;