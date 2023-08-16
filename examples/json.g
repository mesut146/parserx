token{
  STRING: [:string:];
  NUMBER: [0-9]+;
  BOOLEAN: "true" | "false";
  LBRACKET: "[";
  RBRACKET: "]";
  LBRACE: "{";
  RBRACE: "}";
  COMMA: ",";
  COLON: ":";
  WS: [ \r\n\t]+ -> skip;
}
%start: val;

obj: "{" (entry ("," entry)*)? "}";
array: "[" (val ("," val)*)? "]";

entry: STRING ":" val;
val: array | STRING | NUMBER | BOOLEAN | obj;