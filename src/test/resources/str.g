token{
  num: [0-9]+;
  str: "\"" ("\\" . | [^\r\n\"])* "\"";
  sh: [:string:];
  #IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
  SHORTCUT: "[:" IDENT ":]";
  BRACKET: "[" ~"]";
}