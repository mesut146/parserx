include "javaLexer.g"//token definitions

@start: compilationUnit;

qname = IDENT ("." IDENT)* ;
typeList = TypeName ("," TypeName)* ;
TypeName = qname generic? ;
generic = "<" (IDENT | generic) ("," IDENT)* ">" ;
modifiers = ("public" | "static" | "abstract" | "final" | "private" | "volatile" | "protected" | "synchronized")+ ;


compilationUnit = packageDecl? importStmt* typeDecl* ;

packageDecl = "package" qname ";" ;
importStmt = "import" "static"? qname ("." "*")? ";" ;

typeDecl= classDecl | enumDecl ;
classDecl= modifiers? ("class" | "interface") IDENT ("extends" qname)? ("implements" typeList)? "{" classBody "}";

enumDecl = modifiers? "enum" IDENT ("implements" typeList)? "{" enumBody "}";
enumBody = enumCons (enumCons)* ";"? member*;
enumCons = IDENT ("(" anony? ")")?;
anony: "{" classBody "}";

classBody = member*;
member = fieldDecl | methodDecl | typeDecl;
fieldDecl = modifiers? TypeName varFrags;

methodDecl = modifiers? generic? (TypeName | "void") IDENT "(" params? ")" ("throws" typeList)? (block | ";");
params = param ("," param)*;
param = "final"? TypeName "..."? IDENT;

arrayBracket = "[" "]";

block = "{" statement* "}";

//---------statements

statement = varDecl |
            exprStmt |
            ifStatement|
            whileStatement |
            doWhileStatement |
            forStatement |
            forEachStatement|
            block |
            throwStatement;

ifStatement = "if" "(" expr ")" statement ("else" statement)?;

whileStatement = "while" "(" expr ")" statement;

doWhileStatement = "do" block "while" "(" expr ")" ";";

forStatement = "for" "(" forInits? ";" expr? ";" updaters? ")" statement;
forInits = TypeName varFrags;
updaters = expr ("," expr)*;

forEachStatement = "for" "(" "final"? TypeName IDENT ":" expr ")" statement;

varDecl = "final"? TypeName varFrags ";";
varFrags = varFrag ("," varFrag)*;
varFrag = IDENT ("=" expr)?;

exprStmt = expr ";";

tryStatement = "try" block (catchStatement | finallyStatement)*;
tryResourcesStatement = "try" "(" varDecl ")" block;
catchStatement = "catch" "(" "final" TypeName IDENT ")" block;
finallyStatement = "finally" block;

throwStatement = "throw" expr;

//---------expressions
PrimitiveType = "int" | "long" | "short" | "float" |  "double" | "byte" | "char";

expr = ClassInstanceCreationExpression 
           | MethodCall
           | FieldAccess
           | Ternary
           | ParExpr
           | ArrayAccess
           | ArrayCreation
           | Literal
           | PostfixExpression
           | UnaryExpression
           | CastExpression
           | InfixExpression
           | InstanceOf
           | Assignment;

MethodCall: (expr ".")? IDENT "(" arg* ")";
arg: "final"? TypeName "..."? IDENT;

FieldAccess: expr "." IDENT;

ClassInstanceCreationExpression:
    "new" TypeName "(" exprList? ")" classBody;

ParExpr = "(" expr ")";
ArrayAccess = expr "[" expr "]";
ArrayCreation = "new" ("[" expr? "]")+ ArrayInit;
ArrayInit = "{" (expr | ArrayInit)* "}";


Literal:
      INTEGER_LITERAL
    | FLOAT_LITERAL
    | BOOLEAN_LITERAL
    | CHAR_LITERAL
    | STRING_LITERAL
    | "null";

PostfixExpression = expr ("++" | "--");

UnaryExpression:
    ("++" | "--" | "~" | "!" | "+" | "-") expr;

CastExpression:
    "(" (PrimitiveType | ReferenceType) ")" expr;
ReferenceType: TypeName;

InfixExpression:
  expr InfixOp expr;

InfixOp: "+" | "-" | "*" | "/" | "%" | "^" | "&" | "|" | "&&" | "||" | "<<" | ">>" | ">>>" | "<" | ">" | "==" | "!=";
InstanceOf = expr "instanceof" ReferenceType ;

Ternary:
    expr "?" expr ":" expr;


Assignment:
    LeftHandSide AssignmentOperator Assignment;

LeftHandSide:
      qname
    | FieldAccess
    | ArrayAccess;

AssignmentOperator:
    "=" | "*=" | "/=" | "%=" | "+=" | "-=" "<<=" | ">>=" | ">>>=" | "&=" | "^=" | "|=";
    
exprList = expr ("," expr)*;
