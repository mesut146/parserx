token{
    #LETTER = [a-zA-Z]; //todo need unicode
    #DIGIT = [0-9];
    #DIGITS = {DIGIT} {DIGIT_OR_UNDERSCORE}*;
    #DIGIT_OR_UNDERSCORE = [_0-9];

    //#string_content = [^\r\n\"];
    //STRING_LITERAL = "\"" {string_content}* "\"";
    //CHAR_LITERAL = "'" .+ "'";

    INTEGER_LITERAL = {DIGITS};
    FLOAT_LITERAL = {INTEGER_LITERAL} "." {INTEGER_LITERAL};

    //IDENT = {LETTER} ({LETTER} | {DIGIT} | "_")*;

    //operators
    //PLUS = "+";
    //MINUS = "-";
}

skip{
    #LINE_COMMENT = "//" [^\r\n]*;
    #BLOCK_COMMENT = "/*" ([^*]* | "*" [^/]*) "/";
    //COMMENT = {LINE_COMMENT} | {BLOCK_COMMENT};
    //WS = [\s\r\n\t];
}