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
    ClassOrInterfaceType_no_ClassOrInterfaceType
|   ClassOrInterfaceType_no_ClassOrInterfaceType ClassOrInterfaceType(ClassOrInterfaceType)+
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
    ElementValue_no_ElementValue
|   ElementValue_no_ElementValue ElementValue(ElementValue)+
;
ElementValueArrayInitializer = (COMMA | ElementValueList | ElementValueList COMMA)+;
ElementValueList:
    ElementValue
|   ElementValue (COMMA ElementValue)+
;
MarkerAnnotation = AT TypeName;
SingleElementAnnotation = AT TypeName LPAREN ElementValue RPAREN;
PackageName:
    PackageName_no_PackageName
|   PackageName_no_PackageName PackageName(PackageName)+
;
TypeName:
    TypeIdentifier
|   PackageOrTypeName DOT TypeIdentifier
;
PackageOrTypeName:
    PackageOrTypeName_no_PackageOrTypeName
|   PackageOrTypeName_no_PackageOrTypeName PackageOrTypeName(PackageOrTypeName)+
;
ConditionalExpression = Identifier;
ClassType(ClassOrInterfaceType):
    DOT TypeIdentifier
|   ClassOrInterfaceType(ClassOrInterfaceType) DOT TypeIdentifier
|   DOT TypeIdentifier TypeArguments
|   ClassOrInterfaceType(ClassOrInterfaceType) DOT TypeIdentifier TypeArguments
|   DOT Annotation+ TypeIdentifier
|   ClassOrInterfaceType(ClassOrInterfaceType) DOT Annotation+ TypeIdentifier
|   DOT Annotation+ TypeIdentifier TypeArguments
|   ClassOrInterfaceType(ClassOrInterfaceType) DOT Annotation+ TypeIdentifier TypeArguments
;
ClassType_no_ClassOrInterfaceType:
    TypeIdentifier
|   TypeIdentifier TypeArguments
|   Annotation+ TypeIdentifier
|   Annotation+ TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
;
InterfaceType(ClassOrInterfaceType) = ClassType(ClassOrInterfaceType);
InterfaceType_no_ClassOrInterfaceType = ClassType_no_ClassOrInterfaceType;
ClassOrInterfaceType(ClassOrInterfaceType):
    ClassType(ClassOrInterfaceType)
|   InterfaceType(ClassOrInterfaceType)
;
ClassOrInterfaceType_no_ClassOrInterfaceType:
    ClassType_no_ClassOrInterfaceType
|   InterfaceType_no_ClassOrInterfaceType
;
ElementValueList(ElementValue):
    ElementValue(ElementValue)
|   (COMMA ElementValue)+
|   ElementValue(ElementValue) (COMMA ElementValue)+
;
ElementValueArrayInitializer(ElementValue):
    (COMMA | ElementValueList | ElementValueList COMMA)*
|   (ElementValueList(ElementValue) | COMMA | ElementValueList(ElementValue) COMMA) (COMMA | ElementValueList | ElementValueList COMMA)*
;
ElementValueArrayInitializer_no_ElementValue:
    COMMA
|   COMMA (COMMA | ElementValueList | ElementValueList COMMA)+
;
ElementValue(ElementValue) = ElementValueArrayInitializer(ElementValue);
ElementValue_no_ElementValue:
    ConditionalExpression
|   ElementValueArrayInitializer_no_ElementValue
|   Annotation
;
PackageName(PackageName):
    DOT Identifier
|   PackageName(PackageName) DOT Identifier
;
PackageName_no_PackageName = Identifier;
PackageOrTypeName(PackageOrTypeName):
    DOT Identifier
|   PackageOrTypeName(PackageOrTypeName) DOT Identifier
;
PackageOrTypeName_no_PackageOrTypeName = Identifier;
