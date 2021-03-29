token{
  DOT: ".";
  IDENT: [a-zA-Z_]+;
  LP: "(";
  RP: ")";
}


//expr: fieldAccess | qname | methodCall;
name: IDENT;
qname: IDENT ("." IDENT)*;
expr: qname | methodCall | fieldAccess;
methodCall: (expr ".")? name "(" ")";
fieldAccess: expr "." name;
//expr: (expr ".")? name "(" ")" | qname;
//expr: (qname | (expr ".")? name "(" ")") ("." name)*;
