token{
  DOT = ".";
  BAR = "|";
  BOPEN = "[";
  BCLOSE = "]";
  LPAREN = "(";
  RPAREN = ")";
  QUES = "?";
  STAR = "*";
  PLUS = "+";
  XOR = "^";
  MINUS = "-";
  ESCAPED = "\\" .;
  CHAR = [^\\];
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
bracket: BOPEN XOR? range+ BCLOSE;
range: rangeChar rangeg1?;
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