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
arg: "final"? Type "..."? IDENT;

FieldAccess: expr "." IDENT;

ClassInstanceCreationExpression:
    "new" ReferenceType "(" exprList? ")" classBody;

ParExpr = "(" expr ")";
ArrayAccess = expr "[" expr "]";
ArrayCreation = "new" ("[" expr? "]")+ ArrayInit?;
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