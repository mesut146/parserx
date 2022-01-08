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

/* rules */
rhs: seq rhsg1*;
rhsg1: BAR seq;
seq: regex+;
regex: simple regexg1?;
regexg1:
    QUES
|   STAR
|   PLUS
;
simple:
    normalChar
|   bracket
|   LPAREN rhs RPAREN
;
bracket: BOPEN ((XOR (range+ (XOR() XOR() range+() | ε (range_XOR_noe(XOR) range* | range_XOR_eps(XOR) ε)))) | (ε range_no_XOR range*)) BCLOSE;
range: rangeChar rangeg1?;
range_no_XOR: rangeChar_no_XOR rangeg1?;
range_XOR(XOR): rangeChar_XOR(XOR) rangeg1?;
range_XOR_eps(XOR): rangeChar_XOR(XOR) ε;
range_XOR_noe(XOR): rangeChar_XOR(XOR) rangeg1;
rangeg1: MINUS rangeChar;
normalChar:
    CHAR
|   ESCAPED
|   MINUS
|   DOT
;
rangeChar:
    CHAR
|   ESCAPED
|   STAR
|   PLUS
|   QUES
|   BAR
|   DOT
|   LPAREN
|   RPAREN
|   XOR
|   MINUS
|   BOPEN
;
rangeChar_no_XOR:
    CHAR
|   ESCAPED
|   STAR
|   PLUS
|   QUES
|   BAR
|   DOT
|   LPAREN
|   RPAREN
|   MINUS
|   BOPEN
;
rangeChar_XOR(XOR): XOR();