include "javaLexer.g"//token definitions

@start: compilationUnit;

modifiers = ("public" | "static" | "abstract" | "final" | "private" | "volatile" | "protected" | "synchronized")+ ;

qname = IDENT ("." IDENT)*;
generic = "<" (IDENT | generic) ("," IDENT)* ">" ;

PrimitiveType = "int" | "long" | "short" | "float" |  "double" | "byte" | "char";
ReferenceType = qname generic? ;
Type= (PrimitiveType | ReferenceType) ("[" "]")*;
typeList = ReferenceType ("," ReferenceType)* ;



compilationUnit = packageDecl? importStmt* typeDecl* ;

packageDecl = "package" qname ";" ;
importStmt = "import" "static"? qname ("." "*")? ";" ;

typeDecl= classDecl | enumDecl ;
classDecl= modifiers? ("class" | "interface") IDENT ("extends" ReferenceType)? ("implements" typeList)? "{" classBody "}";

enumDecl = modifiers? "enum" IDENT ("implements" typeList)? "{" enumBody "}";
enumBody = enumCons ("," enumCons)* ";"? member*;
enumCons = IDENT (("(" args? ")") anony?)?;
anony: "{" classBody "}";

classBody = member*;
member = fieldDecl | methodDecl | typeDecl;
fieldDecl = modifiers? Type varFrags;

methodDecl = modifiers? generic? (Type | "void") IDENT "(" params? ")" ("throws" typeList)? (block | ";");
params = param ("," param)*;
param = "final"? Type "..."? IDENT;

arrayBracket = "[" "]";




varDecl = "final"? Type varFrags ";";
varFrags = varFrag ("," varFrag)*;
varFrag = IDENT ("=" expr)?;



