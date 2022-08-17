tokens{
  LT: "<" -> tag;
  tag{
    TAGNAME: [a-z]+ -> attr;
  }
  attr{
    GT: ">" -> DEFAULT;
    ATTR: [a-z]+ -> attr2;
    SP: [\s\t]+ -> SKIP;
  }
  attr2{
    EQ: "=";
  }

  EQ: "=";
  STR: "\"" ("\\" . | [^\r\n"])* "\"";
  COMMENT: "<!--" ("-" "-" [^>] | "-" [^-] | [^-])* "-->"
  name: [a-zA-z] [a-zA-z0-9_]*;
  //Whitespace
  S  ::=  ("\\u0020" | "\u0009" | "\u000D" | "\u000A")+
  declStart: "<?xml";
  declEnd: "?>";
  VersionNum:  "1.0";
}
