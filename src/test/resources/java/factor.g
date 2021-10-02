include "lexer-jls.g"

TypeIdentifier = Identifier;

Type:
    PrimitiveType
|   ReferenceType
;
PrimitiveType:
    NumericType
|   BOOLEAN_KEYWORD
|   Annotation+ (NumericType | BOOLEAN_KEYWORD)
;
NumericType:
    IntegralType
|   FloatingPointType
;
IntegralType:
    BYTE_KEYWORD
|   SHORT_KEYWORD
|   INT_KEYWORD
|   LONG_KEYWORD
|   CHAR_KEYWORD
;
FloatingPointType:
    FLOAT_KEYWORD
|   DOUBLE_KEYWORD
;
ReferenceType:
    ClassOrInterfaceType
|   TypeVariable
|   ArrayType
;
ClassOrInterfaceType:
    ClassType
|   InterfaceType
;
ClassType:
    TypeIdentifier
|   TypeIdentifier TypeArguments
|   Annotation+ TypeIdentifier
|   Annotation+ TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
|   ClassOrInterfaceType DOT TypeIdentifier
|   ClassOrInterfaceType DOT TypeIdentifier TypeArguments
|   ClassOrInterfaceType DOT Annotation+ TypeIdentifier
|   ClassOrInterfaceType DOT Annotation+ TypeIdentifier TypeArguments
;
InterfaceType = ClassType;

TypeVariable:
    TypeIdentifier
|   Annotation+ TypeIdentifier
;
Dims:
    LBRACKET RBRACKET
|   LBRACKET RBRACKET (LBRACKET RBRACKET | Annotation+ LBRACKET RBRACKET)+
|   Annotation+ LBRACKET RBRACKET
|   Annotation+ LBRACKET RBRACKET (LBRACKET RBRACKET | Annotation+ LBRACKET RBRACKET)+
;
ArrayType:
    PrimitiveType Dims
|   ClassOrInterfaceType Dims
|   TypeVariable Dims
;

TypeArguments = LT TypeArgumentList GT;
TypeArgumentList:
    TypeArgument
|   TypeArgument (COMMA TypeArgument)+
;
TypeArgument:
    ReferenceType
|   Wildcard
;
Wildcard:
    QUESTION
|   QUESTION WildcardBounds
|   Annotation+ QUESTION
|   Annotation+ QUESTION WildcardBounds
;
WildcardBounds:
    EXTENDS_KEYWORD ReferenceType
|   SUPER_KEYWORD ReferenceType
;

Annotation:
    NormalAnnotation
|   MarkerAnnotation
|   SingleElementAnnotation
;
NormalAnnotation:
    AT TypeName LPAREN RPAREN
|   AT TypeName LPAREN ElementValuePairList RPAREN
;
ElementValuePairList:
    ElementValuePair
|   ElementValuePair (COMMA ElementValuePair)+
;
ElementValuePair = Identifier EQ ElementValue;
ElementValue:
    ConditionalExpression
|   ElementValueArrayInitializer
|   Annotation
;
ElementValueArrayInitializer = (COMMA | ElementValueList | ElementValueList COMMA)+;
ElementValueList:
    ElementValue
|   ElementValue (COMMA ElementValue)+
;
MarkerAnnotation = AT TypeName;
SingleElementAnnotation = AT TypeName LPAREN ElementValue RPAREN;


PackageName:
    Identifier
|   PackageName DOT Identifier
;
TypeName:
    TypeIdentifier
|   PackageOrTypeName DOT TypeIdentifier
;
PackageOrTypeName:
    Identifier
|   PackageOrTypeName DOT Identifier
;

ConditionalExpression: Identifier;