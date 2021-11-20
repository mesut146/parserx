token{
 NUM: [0-9]+;
 IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
 DOT: ".";
 POW: "^";
 PLUS: "+";
 MINUS: "-";
 MUL: "*";
 DIV: "/";
 LP: "(";
 RP: ")";
 LBRACKET: "[";
 RBRACKET: "]";
 QUES: "?";
 COLON: ":";
 TILDE: "~";
 BANG: "!";
 LT: "<";
 GT: ">";
 LTLT: "<<";
 GTGT: ">>";
 GTGTGT: ">>>";
 iof: "instanceof";
 PLUSPLUS: "++";
 MINUSMINUS: "--";

 PERCENT: "%";
 AND: "&";
 OR: "|";
 ANDAND: "&&";
 OROR: "||";

 //comp
 LTEQ: "<=";
 GTEQ: ">=";
 EQEQ: "==";
 NOTEQ: "!=";

 //eq
 eq: "=";
 PLUSEQ: "+=";
 MINUSEQ: "-=";
 MULEQ: "*=";
 DIVEQ: "/=";
 PERCENTEQ: "%=";
 ANDTEQ: "&=";
 POWEQ: "^=";
 OREQ: "|=";
 LTLTEQ: "<<=";
 GTGTEQ: ">>=";
 GTGTGTEQ: ">>>=";
}

/*E:
   E "^" E %left
 | "-" E
 | E ("*" | "/") E %left
 | E ("+" | "-") E %right
 | E "?" E ":" E %right
 | "(" E ")"
 | NUM;
*/

//E: E "++" | "-" E | E "+" E %right | NUM;



E:
  E "." E %left
| E "[" E "]"
| E ("++" | "--") #post
| ("+" | "-" | "++" | "--" | "!" | "~" | "(" IDENT ")") E #unary
| E ("*" | "/" | "%") E %left
| E ("+" | "-") E %left
| E ("<<" | ">>" | ">>>") E %left
| E ("<" | ">" | "<=" | ">=" | "instanceof") E %left
| E ("==" | "!=") E
| E "&" E %left
| E "^" E %left
| E "|" E %left
| E "&&" E %left
| E "||" E %left
| E "?" E ":" E %right
| E ("=" | "+=" | "-=" | "*=" | "/=" | "%=" | "&=" | "^=" | "|=" | "<<=" | ">>=" | ">>>=") E %right
| NUM
| "(" E ")"
;
