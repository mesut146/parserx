token{
  LBRACKET: "[" "^"? -> in_set;

  in_set{
    UNICODE: "\\u" [0-9a-fA-F]+;
    ESCAPED: "\\" .;
    RANGE: [a-z] "-" [a-z] |
           [A-Z] "-" [A-Z] |
           [0-9] "-" [0-9];
    UNICODE_RANGE: UNICODE "-" UNICODE;
    RBRACKET: "]" -> DEFAULT;
    ANY: [\u0000-\uffff];
  }
}