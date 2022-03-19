token{
  DOT: ".";
  BAR: "|";
  BOPEN: "[";
  BCLOSE: "]";
  LPAREN: "(";
  RPAREN: ")";
  QUES: "?";
  STAR: "*";
  PLUS: "+";
  XOR: "^";
  MINUS: "-";
  ESCAPED: "\\" .;
  CHAR: [^\\];
}


rhs: seq ("|" seq)*;
seq: regex+;
regex: simple ("?" | "*" | "+")?;
simple: normalChar | bracket | "(" rhs ")";
normalChar: CHAR | ESCAPED | "-" | ".";
bracket: "[" "^"? range+ "]";
bracket: "[" "^" rangeAll+ "]" | "[" rangeNop rangeAll* "]";
rangeAll: rangeChar ("-" rangeChar)?;
rangeNop: rangeChar2 ("-" rangeChar2)?

rangeChar: CHAR | ESCAPED | "*" | "+" | "?" | "|" | "." | "(" | ")"  | "-" | "[";
rangeChar2: rangeChar | "^";