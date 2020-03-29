
/* blk

*/
token ident ;
token number ;
token public static abstract final private protected ;
token package import ;
token class interface enum ;
token dot semi ;


asd = rule? ass;

/*bb = a b c
    |d e f
    |x y z*;*/

compilationUnit= packageDecl? imports? typeDecl* ;

packageDecl= package qname semi ;

imports= importStmt+ ;
importStmt= import qname (dot star)? semi ;

qname= ident (dot ident)* ;
typeName= qname generic? ;
generic= lt (ident | generic) (semi ident)* gt ;

typeDecl= classDecl | enumDecl ;
classDecl= modifiers? (class|interface) ident (extends qname)? (implements ifaceList)?
ifaceList= qname (comma qname)* ;

modifiers: (public | static | abstract | final | private)+ ;

// qname: ident | full
// full: ident dot full
