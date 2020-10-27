token{
  STRING: "\"";
  NUMBER: [0-9]+;
  BOOLEAN: "true" | "false";
  LBRACKET: "[";
  RBRACKET: "]";
  LBRACE: "{";
  RBRACE: "}";
  COMMA: ",";
  COLON: ":";
}
@start = obj;

obj: "{" (entry ",")* entry? "}";
array: "[" (val ",")* val? "]";

entry: key ":" val;
key: STRING;
val: STRING | NUMBER | BOOLEAN | obj;