include "lexer.g"//token definitions

qname = IDENT ("." IDENT)* ;
typeList= qname ("," qname)* ;
typeName= qname generic? ;
generic= "<" (IDENT | generic) ("," IDENT)* ">" ;
modifiers: ("public" | "static" | "abstract" | "final" | "private" | "volatile" | "protected")+ ;


compilationUnit = packageDecl? imports? typeDecl* ;

packageDecl = "package" qname ";" ;

imports = importStmt+ ;
importStmt = "import" "static"? qname ("." "*")? ";" ;

typeDecl= classDecl | enumDecl ;
classDecl= modifiers? ("class" | "interface") IDENT ("extends" qname)? ("implements" typeList)? "{" classBody "}";

enumDecl = modifiers? "enum" ("extends" qname)? ("implements" typeList)? "{" enumBody "}";

classBody = member*;
member = fieldDecl | methodDecl;
fieldDecl = modifiers? typeName IDENT ("=" expr);
methodDecl = modifiers? IDENT "("  ")";

enumBody = "enum";

expr = IDENT;

// qname: ident | full
// full: ident "." full
