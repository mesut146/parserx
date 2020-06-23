tokens{
    #LETTER = [a-zA-Z] //todo need unicode
    #DIGIT = [0-9]
    #DIGIT_OR_UNDERSCORE = [0-9_]
    #DIGITS = {DIGIT} {DIGIT_OR_UNDERSCORE}*
    #HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]
    #string_content = [^\r\n\"]

    STRING_LITERAL = "\"" {string_content}* "\""
    CHAR_LITERAL = "'" .+ "'"

    INTEGER_LITERAL = {DIGITS} | {HEX_INTEGER_LITERAL} | {BIN_INTEGER_LITERAL} | {OCTAL}
    LONG_LITERAL = {INTEGER_LITERAL} [Ll]
    //INT_OR_LONG = {INTEGER_LITERAL} [Ll]?
    #HEX_INTEGER_LITERAL = "0" [Xx] {HEX_DIGIT_OR_UNDERSCORE}+
    #BIN_INTEGER_LITERAL = "0" [Bb] [01_]+
    #OCTAL= "0" [0-7]+

    NULL_LITERAL = "null"

    TRUE_KEYWORD = "true"
    FALSE_KEYWORD = "false"

    ABSTRACT_KEYWORD = "abstract"
    ASSERT_KEYWORD = "assert"
    BOOLEAN_KEYWORD = "boolean"
    BREAK_KEYWORD = "break"
    BYTE_KEYWORD = "byte"
    CASE_KEYWORD = "case"
    CATCH_KEYWORD = "catch"
    CHAR_KEYWORD = "char"
    CLASS_KEYWORD = "class"
    CONST_KEYWORD = "const"
    CONTINUE_KEYWORD = "continue"
    DEFAULT_KEYWORD = "default"
    DO_KEYWORD = "do"
    DOUBLE_KEYWORD = "double"
    ELSE_KEYWORD = "else"
    ENUM_KEYWORD = "enum"
    EXTENDS_KEYWORD = "extends"
    FINAL_KEYWORD = "final"
    FINALLY_KEYWORD = "finally"
    FLOAT_KEYWORD = "float"
    FOR_KEYWORD = "for"
    GOTO_KEYWORD = "goto"
    IF_KEYWORD = "if"
    IMPLEMENTS_KEYWORD = "implements"
    IMPORT_KEYWORD = "import"
    INSTANCEOF_KEYWORD = "instanceof"
    INT_KEYWORD = "int"
    INTERFACE_KEYWORD = "interface"
    LONG_KEYWORD = "long"
    NATIVE_KEYWORD = "native"
    NEW_KEYWORD = "new"
    PACKAGE_KEYWORD = "package"
    PRIVATE_KEYWORD = "private"
    PROTECTED_KEYWORD = "protected"
    PUBLIC_KEYWORD = "public"
    RETURN_KEYWORD = "return"
    SHORT_KEYWORD = "short"
    STATIC_KEYWORD = "static"
    STRICTFP_KEYWORD = "strictfp"
    SUPER_KEYWORD = "super"
    SWITCH_KEYWORD = "switch"
    SYNCHRONIZED_KEYWORD = "synchronized"
    THIS_KEYWORD = "this"
    TRY_KEYWORD = "try"
    THROW_KEYWORD = "throw"
    THROWS_KEYWORD = "throws"
    TRANSIENT_KEYWORD = "transient"
    WHILE_KEYWORD = "while"
    VOID_KEYWORD = "void"
    VOLATILE_KEYWORD = "volatile"

    IDENT = {LETTER} ({LETTER} | {DIGIT} | "_")*

    DOT = "."
    COMMA = ","
    SEMI = ";"
    COLON = ":"
    LPAREN = "("
    RPAREN = ")"
    LBRACE = "{"
    RBRACE = "}"
    LBRACKET = "["
    RBRACKET = "]"
    BANG = "!"
    SINGLE_QUOTE = "'"
    DOUBLE_QUOTE = "\""
    UNDERSCORE = "_"
    QUESTION = "?"
    EQ = "="
    VARARGS = "..."

    //operators
    PLUS = "+"
    MINUS = "-"
    DIV = "/"
    STAR = "*"
    PERCENT = "%"
    AND = "&"
    ANDAND = "&&"
    OR = "|"
    OROR = "||"
    XOR = "^"
    TILDE = "~"
    PLUSPLUS = "++"
    MINUSMINUS = "--"

    //operator assignments
    PLUSEQ = "+="
    MINUSEQ = "-="
    STAREQ = "*="
    DIVEQ = "/="
    ANDEQ = "&="
    OREQ = "|="
    PERCENTEQ = "%="

    //relational operators
    EQEQ = "=="
    NEQ = "!="
    LT = "<"
    GT = ">"
    LTLT = "<<"
    GTGT = ">>"
    GTGTGT = ">>>"
    LTEQ = "<="
    GTEQ = ">="
    LTLTEQ = "<<="
    GTGTEQ = ">>="
    GTGTGTEQ = ">>>="
}

skip{
    #LINE_COMMENT = "//" [^\r\n]*
    #BLOCK_COMMENT = "/*" ([^*]* | "*" [^/]*) "/"
    COMMENT = {LINE_COMMENT} | {BLOCK_COMMENT}
    WS = [\s\r\n\t]
}