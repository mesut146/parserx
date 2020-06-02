
/*
  blk comment
*/

tokens{
    #LETTER = [a-zA-Z] //need unicode
    #DIGIT = [0-9]
    #DIGIT_OR_UNDERSCORE = [0-9_]
    #DIGITS = {DIGIT} {DIGIT_OR_UNDERSCORE}*
    #HEX_DIGIT_OR_UNDERSCORE = [_0-9A-Fa-f]


    #string_content = [^\r\n\"]
    STRING_LITERAL = "\"" {string_content}* "\""
    CHAR_LITERAL = "'" .+ "'"
    //#ANY = [\u0000-\u0009\u000b-\uffff] //equal to dot
    //SEQ =  "a" "x"+ "b"

    INTEGER_LITERAL = {DIGITS} | {HEX_INTEGER_LITERAL} | {BIN_INTEGER_LITERAL} | {OCTAL}
    LONG_LITERAL = {INTEGER_LITERAL} [Ll]
    //INT_OR_LONG = {INTEGER_LITERAL} [Ll]?
    #HEX_INTEGER_LITERAL = "0" [Xx] {HEX_DIGIT_OR_UNDERSCORE}+
    #BIN_INTEGER_LITERAL = "0" [Bb] [01_]+
    #OCTAL= "0" [0-7]+

    NULL_LITERAL = "null"
    
    /*TRUE_KEYWORD = "true"
    FALSE_KEYWORD = "false"

    ABSTRACT_KEYWORD = "abstract"
    PUBLIC_KEYWORD = "public"
    PRIVATE_KEYWORD = "private"
    PROTECTED_KEYWORD = "protected"
    STATIC_KEYWORD = "static"
    TRANSIENT_KEYWORD = "transient"
    VOLATILE_KEYWORD = "volatile"
    VOID_KEYWORD = "void"*/
    


    IDENT = {LETTER} ({LETTER} | {DIGIT} | "_")*
}

skip{
    #LINE_COMMENT = "//" [^\r\n]*
    #BLOCK_COMMENT = "/*" .* "*/"
    //COMMENT = {LINE_COMMENT} | {BLOCK_COMMENT}
    //WS = [\s\r\n\t]
}



compilationUnit= packageDecl? imports? typeDecl* ;

packageDecl= package qname semi ;

imports= importStmt+ ;
importStmt= import qname (dot star)? semi ;
//ig=

qname= ident (dot ident)* ;
typeName= qname generic? ;
generic= lt (ident | generic) (semi ident)* gt ;

typeDecl= classDecl | enumDecl ;
classDecl= modifiers? (class|interface) ident (extends qname)? (implements ifaceList)?;
ifaceList= qname (comma qname)* ;

modifiers: (public | static | abstract | final | private)+ ;

// qname: ident | full
// full: ident dot full
