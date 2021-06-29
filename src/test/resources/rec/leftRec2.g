token{
  DOT: ".";
  IDENT: [a-zA-Z_]+;
  LP: "(";
  RP: ")";
}

name: IDENT;
qname: IDENT ("." IDENT)*;

expr: qname | methodCall | fieldAccess;
methodCall: (expr ".")? name "(" ")";
fieldAccess: expr "." name;

//methodCall: expr "." name "(" ")" | name "(" ")";
/*expr: IDENT expr(IDENT);
expr0: methodCall0 | fieldAccess0;
expr(IDENT): qname(IDENT) | methodCall(IDENT) | methodCall0 | fieldAccess(IDENT) | fieldAccess0;
qname(IDENT): ("." IDENT)*;
methodCall(IDENT): expr(IDENT) "." name "(" ")" | expr0 "." name "(" ")" | name(IDENT) "(" ")";
methodCall0: expr0 "." name "(" ")";
fieldAccess(IDENT): expr(IDENT) "." name;
fieldAccess0: expr0 "." name;*/