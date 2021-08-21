token{
  NUMBER: [0-9]+;
  LP: "(";
  RP: ")";
  PLUS: "+";
  MINUS: "-";
  MUL: "*";
  DIV: "/";
}

expr: mul (("+" | "-") mul)*;
mul: atom (("*" | "/") atom)*;
atom: "(" expr ")" | "-" atom | NUMBER;