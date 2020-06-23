
include "lexer.g"

/*
  blk comment
*/


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
