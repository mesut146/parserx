include "../common.g"

token{
  IDENT: [a-z]+;
  EQ: "=";
  LP: "(";
  RP: ")";
  COMMA: ",";
}

eq: lhs "=" expr;
lhs: var | funcCall;
expr: var m | d funcCall n | c;
funcCall: IDENT "(" args? ")";
arg: eq | expr;
args: arg rest=("," arg)*;
var: IDENT;
