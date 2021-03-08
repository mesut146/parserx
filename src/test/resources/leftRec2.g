token{
  dot: ".";
  ident: [a-zA-Z_]+;
  lp: "(";
  rp: ")";
}


expr: fieldAccess | name | methodCall;
name: ident;
qname: ident ("." ident)*;
methodCall: (expr ".")? name "(" ")";
fieldAccess: expr "." name;

/*
expr: expr "." name | name | methodCall;

expr: name expr' | methodCall expr';
expr': "." name expr' | E;

expr: name expr' | (expr ".")? name "(" ")" expr';
expr: name expr' expr''
expr'':

*/