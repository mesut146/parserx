tokens{
LETTER = [a-zA-z] //maybe [^\r\n] is better
DIGIT = [0-9]

STRING_LITERAL = "\"" [^\r\n\"]* "\""
CHAR_LITERAL = "'" .* "'"
INTEGER_LITERAL = [1-9] {DIGIT}*
HEX_LITERAL = "0" [xX] ({DIGIT} | [a-f])+
LONG_LITERAL = {INTEGER_LITERAL} [lL]
//FLOAT_LITERAL = {INTEGER_LITERAL} "." {INTEGER_LITERAL}
//DOUBLE_LITERAL = {FLOAT_LITERAL} [dD]

NULL_LITERAL = "null"


IDENT = {LETTER} ({LETTER} | {DIGIT} | "_")*
}

skip{
LINE_COMMENT = "//" [^\r\n]*
BLOCK_COMMENT = "/*" .* "*/"
COMMENT = {LINE_COMMENT} | {BLOCK_COMMENT}
WS = [ \r\n\t]
}


