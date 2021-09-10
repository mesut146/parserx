token{
  DOT: ".";
  IDENT: [a-zA-Z_]+;
  LP: "(";
  RP: ")";
}

name: IDENT;
qname: IDENT ("." IDENT)*;

expr: qname | methodCall | fieldAccess;
methodCall: scope? name "(" ")";
scope: expr ".";
fieldAccess: scope name;

/*

*/