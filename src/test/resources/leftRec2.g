token{
  dot: ".";
  ident: [a-zA-Z_]+;
  lp: "(";
  rp: ")";
}


//expr: fieldAccess | qname | methodCall;
name: ident;
qname: ident ("." ident)*;
//methodCall: (expr ".")? name "(" ")";
//fieldAccess: expr "." name;

//expr: (qname | (expr ".")? name "(" ")") ("." name)*;

//expr: expr "." name | qname | methodCall;
//methodCall: "(" ")";

//expr: (qname | methodCall) ("." name)*;
//expr: (qname | (expr ".")? name "(" ")") ("." name)*;
expr: (name "(" ")" | qname) ("." name)* ("." name "(" ")" ("." name)*)*;