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
bracket: "[" "^"? range+ "]";
range: rangeChar ("-" rangeChar)?;
normalChar: CHAR | ESCAPED | "-" | ".";
rangeChar: CHAR | ESCAPED | "*" | "+" | "?" | "|" | "." | "(" | ")" | "^" | "-" | "[";