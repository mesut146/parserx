token{
    StringLiteral : [:string:];
    CharacterLiteral : "'" [^']+ "'";
    TextBlock : "\"\"\"" ~"\"\"\"";

    BooleanLiteral : "true" | "false";
    NullLiteral : "null";

    IntegerLiteral: DecimalIntegerLiteral | HexIntegerLiteral | OctalIntegerLiteral | BinaryIntegerLiteral;
    #DecimalIntegerLiteral:  DecimalNumeral IntegerTypeSuffix?;
    #HexIntegerLiteral:  HexNumeral IntegerTypeSuffix?;
    #OctalIntegerLiteral:  OctalNumeral IntegerTypeSuffix?;
    #BinaryIntegerLiteral:  BinaryNumeral IntegerTypeSuffix?;
    #IntegerTypeSuffix: [Ll];

    #DecimalNumeral:  "0" | NonZeroDigit Digits? | NonZeroDigit Underscores Digits;
    #NonZeroDigit: [0-9];
    #Digits: Digit | Digit DigitsAndUnderscores? Digit;
    #Digit: "0" | NonZeroDigit;
    #DigitsAndUnderscores:  DigitOrUnderscore DigitOrUnderscore*;
    #DigitOrUnderscore:  Digit | "_" ;
    #Underscores:  "_" "_"*;

    #HexNumeral:  "0" "x" HexDigits | "0" "X" HexDigits;
    #HexDigits:  HexDigit | HexDigit HexDigitsAndUnderscores? HexDigit;
    #HexDigit: [0-9a-fA-F];
    #HexDigitsAndUnderscores: HexDigitOrUnderscore HexDigitOrUnderscore*;
    #HexDigitOrUnderscore:  HexDigit | "_";

    #OctalNumeral:  "0" OctalDigits | "0" Underscores OctalDigits;
    #OctalDigits:  OctalDigit | OctalDigit OctalDigitsAndUnderscores? OctalDigit;
    #OctalDigit: [0-7];
    #OctalDigitsAndUnderscores: OctalDigitOrUnderscore OctalDigitOrUnderscore*;
    #OctalDigitOrUnderscore: OctalDigit | "_";

    #BinaryNumeral:  "0" [bB] BinaryDigits;
    #BinaryDigits:  BinaryDigit | BinaryDigit BinaryDigitsAndUnderscores? BinaryDigit;
    #BinaryDigit: [01];
    #BinaryDigitsAndUnderscores:  BinaryDigitOrUnderscore BinaryDigitOrUnderscore*;
    #BinaryDigitOrUnderscore:  BinaryDigit | "_";

    #FloatingPointLiteral:  DecimalFloatingPointLiteral | HexadecimalFloatingPointLiteral;
    #DecimalFloatingPointLiteral:
       Digits "." Digits? ExponentPart? FloatTypeSuffix?
     | "." Digits ExponentPart? FloatTypeSuffix?
     | Digits ExponentPart FloatTypeSuffix?
     | Digits ExponentPart? FloatTypeSuffix;
    #ExponentPart: [eE] SignedInteger;
    #SignedInteger:  [+-]? Digits;
    #FloatTypeSuffix: [fFdD];

    #HexadecimalFloatingPointLiteral: HexSignificand BinaryExponent FloatTypeSuffix?;
    #HexSignificand:  HexNumeral "."? | "0" [xX] HexDigits? "." HexDigits;
    #BinaryExponent:  [pP] SignedInteger;
}

token{
    ABSTRACT_KEYWORD : "abstract";
    ASSERT_KEYWORD : "assert";
    BOOLEAN_KEYWORD : "boolean";
    BREAK_KEYWORD : "break";
    BYTE_KEYWORD : "byte";
    CASE_KEYWORD : "case";
    CATCH_KEYWORD : "catch";
    CHAR_KEYWORD : "char";
    CLASS_KEYWORD : "class";
    CONST_KEYWORD : "const";
    CONTINUE_KEYWORD : "continue";
    DEFAULT_KEYWORD : "default";
    DO_KEYWORD : "do";
    DOUBLE_KEYWORD : "double";
    ELSE_KEYWORD : "else";
    ENUM_KEYWORD : "enum";
    EXPORTS_KW : "exports";
    EXTENDS_KEYWORD : "extends";
    FINAL_KEYWORD : "final";
    FINALLY_KEYWORD : "finally";
    FLOAT_KEYWORD : "float";
    FOR_KEYWORD : "for";
    GOTO_KEYWORD : "goto";
    IF_KEYWORD : "if";
    IMPLEMENTS_KEYWORD : "implements";
    IMPORT_KEYWORD : "import";
    INSTANCEOF_KEYWORD : "instanceof";
    INT_KEYWORD : "int";
    INTERFACE_KEYWORD : "interface";
    LONG_KEYWORD : "long";
    MODULE_KW : "module";
    NATIVE_KEYWORD : "native";
    NEW_KEYWORD : "new";
    OPEN_KEYWORD : "open";
    OPENS_KW : "opens";
    PACKAGE_KEYWORD : "package";
    PRIVATE_KEYWORD : "private";
    PROTECTED_KEYWORD : "protected";
    PROVIDES_KW : "provides";
    PUBLIC_KEYWORD : "public";
    RECORD_KW : "record";
    RETURN_KEYWORD : "return";
    REQUIRES_KW : "requires";
    SHORT_KEYWORD : "short";
    STATIC_KEYWORD : "static";
    STRICTFP_KEYWORD : "strictfp";
    SUPER_KEYWORD : "super";
    SWITCH_KEYWORD : "switch";
    SYNCHRONIZED_KEYWORD : "synchronized";
    THIS_KEYWORD : "this";
    TO_KW : "to";
    TRANSITIVE_KW : "transitive";
    TRY_KEYWORD : "try";
    THROW_KEYWORD : "throw";
    THROWS_KEYWORD : "throws";
    TRANSIENT_KEYWORD : "transient";
    USES : "uses";
    WHILE_KEYWORD : "while";
    WITH_KW : "with";
    VAR_KW : "var";
    VOID_KEYWORD : "void";
    VOLATILE_KEYWORD : "volatile";
    YIELD_KW : "yield";

    #LETTER : [a-zA-Z]; //todo need unicode
    Identifier : LETTER (LETTER | Digit | "_")*;

    DOT : ".";
    COMMA : ",";
    SEMI : ";";
    COLON : ":";
    LPAREN : "(";
    RPAREN : ")";
    LBRACE : "{";
    RBRACE : "}";
    LBRACKET : "[";
    RBRACKET : "]";
    BANG : "!";
    UNDERSCORE : "_";
    QUESTION : "?";
    EQ : "=";
    VARARGS : "...";
    AT : "@";
    ARROW : "->";
    COLONCOLON : "::";

    //operators
    PLUS : "+";
    MINUS : "-";
    DIV : "/";
    STAR : "*";
    PERCENT : "%";
    AND : "&";
    ANDAND : "&&";
    OR : "|";
    OROR : "||";
    XOR : "^";
    TILDE : "~";
    PLUSPLUS : "++";
    MINUSMINUS : "--";

    //operator assignments
    PLUSEQ : "+=";
    MINUSEQ : "-=";
    STAREQ : "*=";
    DIVEQ : "/=";
    ANDEQ : "&=";
    OREQ : "|=";
    PERCENTEQ : "%=";
    POWEQ : "^=";

    //relational operators
    EQEQ : "==";
    NEQ : "!=";
    LT : "<";
    GT : ">";
    LTLT : "<<";
    //GTGT : ">>";
    //GTGTGT : ">>>";
    LTEQ : "<=";
    GTEQ : ">=";
    LTLTEQ : "<<=";
    GTGTEQ : ">>=";
    GTGTGTEQ : ">>>=";
}

token{
    LINE_COMMENT : "//" [^\r\n]* -> skip;
    BLOCK_COMMENT : "/*" ([^*]* | "*" [^/]*) "/" -> skip;
    WS : [\s\r\n\t] -> skip;
}
