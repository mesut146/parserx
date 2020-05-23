
/*
  blk comment
*/

tokens{
    #LETTER = [a-zA-Z]
    #DIGIT = [0-9]
    #DIGIT_OR_UNDERSCORE = {DIGIT} | "_"
    STRING_LITERAL = "\"" ("\\\"" | [^\r\n\"])* "\""
    CHAR_LITERAL = "'" [^\']* "'"
    INTEGER_LITERAL = [1-9] {DIGIT_OR_UNDERSCORE}*
    HEX_LITERAL = "0" [xX] ({DIGIT} | [a-f])+
    LONG_LITERAL = {INTEGER_LITERAL} [lL]
    //FLOAT_LITERAL = {INTEGER_LITERAL} "." {INTEGER_LITERAL}
    //DOUBLE_LITERAL = {FLOAT_LITERAL} [dD]

    NULL_LITERAL = "null"
    PUBLIC_KEYWORD = "public"

    IDENT = {LETTER} ({LETTER} | {DIGIT} | "_")*
}

skip{
    #LINE_COMMENT = "//" [^\r\n]*
    #BLOCK_COMMENT = "/*" .* "*/"
    COMMENT = {LINE_COMMENT} | {BLOCK_COMMENT}
    WS = [\s\r\n\t]
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
