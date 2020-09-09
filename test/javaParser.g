include "javaLexer.g"//token definitions

qname = IDENT ("." IDENT)* ;
typeList = typeName ("," typeName)* ;
typeName = qname generic? ;
generic = "<" (IDENT | generic) ("," IDENT)* ">" ;
modifiers = ("public" | "static" | "abstract" | "final" | "private" | "volatile" | "protected" | "synchronized")+ ;


compilationUnit = packageDecl? imports? typeDecl* ;

packageDecl = "package" qname ";" ;

imports = importStmt+ ;
importStmt = "import" "static"? qname ("." "*")? ";" ;

typeDecl= classDecl | enumDecl ;
classDecl= modifiers? ("class" | "interface") IDENT ("extends" qname)? ("implements" typeList)? "{" classBody "}";

enumDecl = modifiers? "enum" ("extends" qname)? ("implements" typeList)? "{" enumBody "}";

classBody = member*;
member = fieldDecl | methodDecl;
fieldDecl = modifiers? typeName varFrags;

methodDecl = modifiers? generic (typeName | "void") IDENT "(" params? ")" ("throws" typeList)? block;
params = param ("," param)*;
param = "final"? typeName "..."? IDENT;
enumBody = "enum";//todo

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

ifStatement = "if" "(" expr ")" statement ("else" (ifStatement | statement))?;

whileStatement = "while" "(" expr ")" statement;

doWhileStatement = "do" block "while" "(" expr ")" ";";

forStatement = "for" "(" forInits? ";" expr? ";" updaters? ")" statement;
forInits = typeName varFrags;
updaters = expr ("," expr)*;

forEachStatement = "for" "(" typeName IDENT ":" expr ")" statement;

varDecl = "final"? typeName varFrags ";";
varFrags = varFrag ("," varFrag)*;
varFrag = IDENT ("=" expr)?;

exprStmt = expr ";";

tryStatement = "try" block (catchStatement | finallyStatement)*;
tryResourcesStatement = "try" "(" varDecl ")" block;
catchStatement = "catch" "(" typeName IDENT ")" block;
finallyStatement = "finally" block;

throwStatement = "throw" expr;

//---------expressions


//Class Instance Creation Expressions
ClassInstanceCreationExpression:
    "new" TypeArguments? TypeDeclSpecifier TypeArgumentsOrDiamond?
                                                            ( ArgumentList? ) ClassBody? |
    Primary "." new TypeArguments? Identifier TypeArgumentsOrDiamond?
                                                            ( ArgumentList? ) ClassBody?;

TypeArgumentsOrDiamond:
    TypeArguments |
    "<" ">";

ArgumentList:
    Expression|
    ArgumentList "," Expression;

//Primary Expressions
Primary:
    PrimaryNoNewArray|
    ArrayCreationExpression;

PrimaryNoNewArray:
    Literal|
    Type "." "class"|
    "void" "." "class"|
    "this"|
    ClassName "." "this"|
    "(" Expression ")"|
    ClassInstanceCreationExpression|
    FieldAccess|
    MethodInvocation|
    ArrayAccess;

//Primary Expressions
Literal:
    IntegerLiteral|
    FloatingPointLiteral|
    BooleanLiteral|
    CHAR_LITERAL|
    STRING_LITERAL|
    "null";
BooleanLiteral: "true" | "false";

PostfixExpression = Primary | ExpressionName | PostIncrementExpression | PostDecrementExpression;

PostIncrementExpression = PostfixExpression "++";
PostDecrementExpression = PostfixExpression "--";

UnaryExpression:
    PreIncrementExpression
    PreDecrementExpression
    "+" UnaryExpression
    "-" UnaryExpression
    UnaryExpressionNotPlusMinus;

PreIncrementExpression:
    "++" UnaryExpression;

PreDecrementExpression:
    "--" UnaryExpression;

UnaryExpressionNotPlusMinus:
    PostfixExpression
    "~" UnaryExpression
    "!" UnaryExpression
    CastExpression;

CastExpression:
    "(" PrimitiveType ")" UnaryExpression
    "(" ReferenceType ")" UnaryExpressionNotPlusMinus;

MultiplicativeExpression:
    UnaryExpression
    MultiplicativeExpression "*" UnaryExpression
    MultiplicativeExpression "/" UnaryExpression
    MultiplicativeExpression "%" UnaryExpression;


AdditiveExpression:
    MultiplicativeExpression
    AdditiveExpression "+" MultiplicativeExpression
    AdditiveExpression "-" MultiplicativeExpression;

ShiftExpression:
    AdditiveExpression
    ShiftExpression "<<" AdditiveExpression
    ShiftExpression ">>" AdditiveExpression
    ShiftExpression ">>>" AdditiveExpression;

RelationalExpression:
    ShiftExpression
    RelationalExpression "<" ShiftExpression
    RelationalExpression ">" ShiftExpression
    RelationalExpression "<=" ShiftExpression
    RelationalExpression ">=" ShiftExpression
    RelationalExpression "instanceof" ReferenceType;


EqualityExpression:
    RelationalExpression
    EqualityExpression "==" RelationalExpression
    EqualityExpression "!=" RelationalExpression;

//Bitwise and Logical Operators

AndExpression:
    EqualityExpression
    AndExpression "&" EqualityExpression;

ExclusiveOrExpression:
    AndExpression
    ExclusiveOrExpression "^" AndExpression;

InclusiveOrExpression:
    ExclusiveOrExpression
    InclusiveOrExpression "|" ExclusiveOrExpression;

//Conditional-And Operator &&
ConditionalAndExpression:
    InclusiveOrExpression
    ConditionalAndExpression "&&" InclusiveOrExpression;

//Conditional-Or Operator ||
ConditionalOrExpression:
    ConditionalAndExpression
    ConditionalOrExpression "||" ConditionalAndExpression;

//Conditional Operator ? :
ConditionalExpression:
    ConditionalOrExpression
    ConditionalOrExpression "?" Expression ":" ConditionalExpression;


//Assignment Operators
AssignmentExpression:
    ConditionalExpression|
    Assignment;

Assignment:
    LeftHandSide AssignmentOperator AssignmentExpression;

LeftHandSide:
    ExpressionName|
    FieldAccess|
    ArrayAccess;

AssignmentOperator:
    "=" | "*=" | "/=" | "%=" | "+=" | "-=" "<<=" | ">>=" | ">>>=" | "&=" | "^=" | "|=";
exprList = expr ("," expr)*;
