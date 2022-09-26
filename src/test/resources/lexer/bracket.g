token{
  LBRACKET: "[" -> in;

  in{
    NOT: "^";
    ESCAPED: "\\" .;
    RBRACKET: "]" -> DEFAULT;
  }
}
[\^[*/-]


normalChar: CHAR | ESCAPED | "-" | ".";
bracket: "[" "^"? range+ "]";
bracket: "[" "^" rangeAll+ "]" | "[" rangeNop rangeAll* "]";
rangeAll: rangeChar ("-" rangeChar)?;
rangeNop: rangeChar2 ("-" rangeChar2)?

rangeChar: CHAR | ESCAPED | "*" | "+" | "?" | "|" | "." | "(" | ")"  | "-" | "[";
rangeChar2: rangeChar | "^";

range: ESCAPED "-"