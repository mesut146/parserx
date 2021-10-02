TypeIdentifier = Identifier;
Type:
    SingleElementAnnotation+ (PrimitiveType(SingleElementAnnotation+) | ReferenceType(SingleElementAnnotation+))
|   PrimitiveType_nop_SingleElementAnnotation
|   ReferenceType_nop_SingleElementAnnotation
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
    SingleElementAnnotation+ (ClassOrInterfaceType(SingleElementAnnotation+) | TypeVariable(SingleElementAnnotation+) | ArrayType(SingleElementAnnotation+))
|   ClassOrInterfaceType_nop_SingleElementAnnotation
|   TypeVariable_nop_SingleElementAnnotation
|   ArrayType_nop_SingleElementAnnotation
;
ClassOrInterfaceType:
    SingleElementAnnotation+ (ClassOrInterfaceType_no_ClassOrInterfaceType(SingleElementAnnotation+) | ClassOrInterfaceType_no_ClassOrInterfaceType(SingleElementAnnotation+) ClassOrInterfaceType (ClassOrInterfaceType)+)
|   ClassOrInterfaceType_no_ClassOrInterfaceType_nop_SingleElementAnnotation
|   ClassOrInterfaceType_no_ClassOrInterfaceType_nop_SingleElementAnnotation ClassOrInterfaceType (ClassOrInterfaceType)+
;
ClassType:
    SingleElementAnnotation+ ((Annotation_no_SingleElementAnnotation Annotation* | ε) (Annotation+ TypeIdentifier | Annotation+ TypeIdentifier TypeArguments) | ClassOrInterfaceType(SingleElementAnnotation+) DOT TypeIdentifier | ClassOrInterfaceType(SingleElementAnnotation+) DOT TypeIdentifier TypeArguments | ClassOrInterfaceType(SingleElementAnnotation+) DOT Annotation+ TypeIdentifier | ClassOrInterfaceType(SingleElementAnnotation+) DOT Annotation+ TypeIdentifier TypeArguments)
|   Annotation_no_SingleElementAnnotation Annotation* (Annotation+ TypeIdentifier | Annotation+ TypeIdentifier TypeArguments)
|   TypeIdentifier
|   TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT TypeIdentifier
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT TypeIdentifier TypeArguments
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT Annotation+ TypeIdentifier
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT Annotation+ TypeIdentifier TypeArguments
;
InterfaceType = ClassType;
TypeVariable:
    TypeIdentifier
|   Annotation+ TypeIdentifier
;
Dims:
    Annotation+ (Annotation+ LBRACKET RBRACKET | Annotation+ LBRACKET RBRACKET (LBRACKET RBRACKET | Annotation+ LBRACKET RBRACKET)+)
|   LBRACKET RBRACKET
|   LBRACKET RBRACKET (LBRACKET RBRACKET | Annotation+ LBRACKET RBRACKET)+
;
ArrayType:
    SingleElementAnnotation+ (PrimitiveType(SingleElementAnnotation+) Dims | ClassOrInterfaceType(SingleElementAnnotation+) Dims | TypeVariable(SingleElementAnnotation+) Dims)
|   PrimitiveType_nop_SingleElementAnnotation Dims
|   ClassOrInterfaceType_nop_SingleElementAnnotation Dims
|   TypeVariable_nop_SingleElementAnnotation Dims
;
TypeArguments = LT TypeArgumentList GT;
TypeArgumentList:
    SingleElementAnnotation+ (TypeArgument(SingleElementAnnotation+) | TypeArgument(SingleElementAnnotation+) (COMMA TypeArgument)+)
|   TypeArgument_nop_SingleElementAnnotation
|   TypeArgument_nop_SingleElementAnnotation (COMMA TypeArgument)+
;
TypeArgument:
    SingleElementAnnotation+ (ReferenceType(SingleElementAnnotation+) | Wildcard(SingleElementAnnotation+))
|   ReferenceType_nop_SingleElementAnnotation
|   Wildcard_nop_SingleElementAnnotation
;
Wildcard:
    Annotation+ (Annotation+ QUESTION | Annotation+ QUESTION WildcardBounds)
|   QUESTION
|   QUESTION WildcardBounds
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
|   ElementValue_no_ElementValue ElementValue (ElementValue)+
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
|   PackageName_no_PackageName PackageName (PackageName)+
;
TypeName:
    TypeIdentifier
|   PackageOrTypeName DOT TypeIdentifier
;
PackageOrTypeName:
    PackageOrTypeName_no_PackageOrTypeName
|   PackageOrTypeName_no_PackageOrTypeName PackageOrTypeName (PackageOrTypeName)+
;
ConditionalExpression = Identifier;
ClassType(ClassOrInterfaceType):
    SingleElementAnnotation+ (ClassOrInterfaceType(SingleElementAnnotation+) ClassOrInterfaceType DOT TypeIdentifier | ClassOrInterfaceType(SingleElementAnnotation+) ClassOrInterfaceType DOT TypeIdentifier TypeArguments | ClassOrInterfaceType(SingleElementAnnotation+) ClassOrInterfaceType DOT Annotation+ TypeIdentifier | ClassOrInterfaceType(SingleElementAnnotation+) ClassOrInterfaceType DOT Annotation+ TypeIdentifier TypeArguments)
|   DOT TypeIdentifier
|   ClassOrInterfaceType_nop_SingleElementAnnotation ClassOrInterfaceType DOT TypeIdentifier
|   DOT TypeIdentifier TypeArguments
|   ClassOrInterfaceType_nop_SingleElementAnnotation ClassOrInterfaceType DOT TypeIdentifier TypeArguments
|   DOT Annotation+ TypeIdentifier
|   ClassOrInterfaceType_nop_SingleElementAnnotation ClassOrInterfaceType DOT Annotation+ TypeIdentifier
|   DOT Annotation+ TypeIdentifier TypeArguments
|   ClassOrInterfaceType_nop_SingleElementAnnotation ClassOrInterfaceType DOT Annotation+ TypeIdentifier TypeArguments
;
ClassType_no_ClassOrInterfaceType:
    Annotation+ (Annotation+ TypeIdentifier | Annotation+ TypeIdentifier TypeArguments)
|   TypeIdentifier
|   TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
;
InterfaceType(ClassOrInterfaceType) = ClassType ClassOrInterfaceType;
InterfaceType_no_ClassOrInterfaceType = ClassType_no_ClassOrInterfaceType;
ClassOrInterfaceType(ClassOrInterfaceType):
    SingleElementAnnotation+ (InterfaceType(SingleElementAnnotation+) ClassOrInterfaceType)
|   InterfaceType_nop_SingleElementAnnotation ClassOrInterfaceType
;
ClassOrInterfaceType_no_ClassOrInterfaceType:
    Annotation+ (InterfaceType_no_ClassOrInterfaceType(Annotation+))
|   InterfaceType_no_ClassOrInterfaceType_nop_Annotation
;
ElementValueList(ElementValue):
    ElementValue ElementValue
|   (COMMA ElementValue)+
|   ElementValue ElementValue (COMMA ElementValue)+
;
ElementValueArrayInitializer(ElementValue):
    (COMMA | ElementValueList | ElementValueList COMMA)*
|   (ElementValueList ElementValue | COMMA | ElementValueList ElementValue COMMA) (COMMA | ElementValueList | ElementValueList COMMA)*
;
ElementValueArrayInitializer_no_ElementValue:
    COMMA
|   COMMA (COMMA | ElementValueList | ElementValueList COMMA)+
;
ElementValue(ElementValue) = ElementValueArrayInitializer ElementValue;
ElementValue_no_ElementValue:
    ConditionalExpression
|   ElementValueArrayInitializer_no_ElementValue
|   Annotation
;
PackageName(PackageName):
    DOT Identifier
|   PackageName PackageName DOT Identifier
;
PackageName_no_PackageName = Identifier;
PackageOrTypeName(PackageOrTypeName):
    DOT Identifier
|   PackageOrTypeName PackageOrTypeName DOT Identifier
;
PackageOrTypeName_no_PackageOrTypeName = Identifier;
Annotation(SingleElementAnnotation) = SingleElementAnnotation(SingleElementAnnotation);
Annotation_no_SingleElementAnnotation:
    NormalAnnotation
|   MarkerAnnotation
;
PrimitiveType(SingleElementAnnotation+) = (Annotation_no_SingleElementAnnotation Annotation* | ε) (NumericType | BOOLEAN_KEYWORD);
PrimitiveType_nop_SingleElementAnnotation:
    NumericType
|   BOOLEAN_KEYWORD
|   Annotation_no_SingleElementAnnotation Annotation* (NumericType | BOOLEAN_KEYWORD)
;
ClassType_no_ClassOrInterfaceType(SingleElementAnnotation+):
    (Annotation_no_SingleElementAnnotation Annotation* | ε) TypeIdentifier
|   (Annotation_no_SingleElementAnnotation Annotation* | ε) TypeIdentifier TypeArguments
;
ClassType_no_ClassOrInterfaceType_nop_SingleElementAnnotation:
    TypeIdentifier
|   TypeIdentifier TypeArguments
|   Annotation_no_SingleElementAnnotation Annotation* TypeIdentifier
|   Annotation_no_SingleElementAnnotation Annotation* TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
;
InterfaceType_no_ClassOrInterfaceType(SingleElementAnnotation+) = ClassType_no_ClassOrInterfaceType(SingleElementAnnotation+);
InterfaceType_no_ClassOrInterfaceType_nop_SingleElementAnnotation = ClassType_no_ClassOrInterfaceType_nop_SingleElementAnnotation;
ClassOrInterfaceType_no_ClassOrInterfaceType(SingleElementAnnotation+):
    ClassType_no_ClassOrInterfaceType(SingleElementAnnotation+)
|   InterfaceType_no_ClassOrInterfaceType(SingleElementAnnotation+)
;
ClassOrInterfaceType_no_ClassOrInterfaceType_nop_SingleElementAnnotation:
    ClassType_no_ClassOrInterfaceType_nop_SingleElementAnnotation
|   InterfaceType_no_ClassOrInterfaceType_nop_SingleElementAnnotation
;
ClassOrInterfaceType(SingleElementAnnotation+):
    ClassOrInterfaceType_no_ClassOrInterfaceType(SingleElementAnnotation+)
|   ClassOrInterfaceType_no_ClassOrInterfaceType(SingleElementAnnotation+) ClassOrInterfaceType (ClassOrInterfaceType)+
;
ClassOrInterfaceType_nop_SingleElementAnnotation:
    ClassOrInterfaceType_no_ClassOrInterfaceType_nop_SingleElementAnnotation
|   ClassOrInterfaceType_no_ClassOrInterfaceType_nop_SingleElementAnnotation ClassOrInterfaceType (ClassOrInterfaceType)+
;
TypeVariable(SingleElementAnnotation+) = (Annotation_no_SingleElementAnnotation Annotation* | ε) TypeIdentifier;
TypeVariable_nop_SingleElementAnnotation:
    TypeIdentifier
|   Annotation_no_SingleElementAnnotation Annotation* TypeIdentifier
;
ArrayType(SingleElementAnnotation+):
    PrimitiveType(SingleElementAnnotation+) Dims
|   ClassOrInterfaceType(SingleElementAnnotation+) Dims
|   TypeVariable(SingleElementAnnotation+) Dims
;
ArrayType_nop_SingleElementAnnotation:
    PrimitiveType_nop_SingleElementAnnotation Dims
|   ClassOrInterfaceType_nop_SingleElementAnnotation Dims
|   TypeVariable_nop_SingleElementAnnotation Dims
;
ReferenceType(SingleElementAnnotation+):
    ClassOrInterfaceType(SingleElementAnnotation+)
|   TypeVariable(SingleElementAnnotation+)
|   ArrayType(SingleElementAnnotation+)
;
ReferenceType_nop_SingleElementAnnotation:
    ClassOrInterfaceType_nop_SingleElementAnnotation
|   TypeVariable_nop_SingleElementAnnotation
|   ArrayType_nop_SingleElementAnnotation
;
SingleElementAnnotation(AT) = AT(AT) TypeName LPAREN ElementValue RPAREN;
Annotation(NormalAnnotation) = NormalAnnotation(NormalAnnotation);
Annotation_no_NormalAnnotation:
    MarkerAnnotation
|   SingleElementAnnotation
;
Wildcard(SingleElementAnnotation+):
    (Annotation_no_SingleElementAnnotation Annotation* | ε) QUESTION
|   (Annotation_no_SingleElementAnnotation Annotation* | ε) QUESTION WildcardBounds
;
Wildcard_nop_SingleElementAnnotation:
    QUESTION
|   QUESTION WildcardBounds
|   Annotation_no_SingleElementAnnotation Annotation* QUESTION
|   Annotation_no_SingleElementAnnotation Annotation* QUESTION WildcardBounds
;
TypeArgument(SingleElementAnnotation+):
    ReferenceType(SingleElementAnnotation+)
|   Wildcard(SingleElementAnnotation+)
;
TypeArgument_nop_SingleElementAnnotation:
    ReferenceType_nop_SingleElementAnnotation
|   Wildcard_nop_SingleElementAnnotation
;
ClassType(SingleElementAnnotation+) = SingleElementAnnotation+ ((Annotation_no_SingleElementAnnotation Annotation* | ε) (Annotation+ TypeIdentifier | Annotation+ TypeIdentifier TypeArguments) | ClassOrInterfaceType(SingleElementAnnotation+) DOT TypeIdentifier | ClassOrInterfaceType(SingleElementAnnotation+) DOT TypeIdentifier TypeArguments | ClassOrInterfaceType(SingleElementAnnotation+) DOT Annotation+ TypeIdentifier | ClassOrInterfaceType(SingleElementAnnotation+) DOT Annotation+ TypeIdentifier TypeArguments);
ClassType_nop_SingleElementAnnotation:
    Annotation_no_SingleElementAnnotation Annotation* (Annotation+ TypeIdentifier | Annotation+ TypeIdentifier TypeArguments)
|   TypeIdentifier
|   TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT TypeIdentifier
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT TypeIdentifier TypeArguments
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT Annotation+ TypeIdentifier
|   ClassOrInterfaceType_nop_SingleElementAnnotation DOT Annotation+ TypeIdentifier TypeArguments
;
InterfaceType(SingleElementAnnotation+) = ClassType(SingleElementAnnotation+);
InterfaceType_nop_SingleElementAnnotation = ClassType_nop_SingleElementAnnotation;
ClassType_no_ClassOrInterfaceType(Annotation+) = Annotation+ (Annotation+ TypeIdentifier | Annotation+ TypeIdentifier TypeArguments);
ClassType_no_ClassOrInterfaceType_nop_Annotation:
    TypeIdentifier
|   TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
;
InterfaceType_no_ClassOrInterfaceType(Annotation+) = ClassType_no_ClassOrInterfaceType(Annotation+);
InterfaceType_no_ClassOrInterfaceType_nop_Annotation = ClassType_no_ClassOrInterfaceType_nop_Annotation;
ElementValue(ElementValue_no_ElementValue):
    ElementValue_no_ElementValue(ElementValue_no_ElementValue)
|   ElementValue_no_ElementValue(ElementValue_no_ElementValue) ElementValue (ElementValue)+
;
ElementValueList(ElementValue_no_ElementValue):
    ElementValue(ElementValue_no_ElementValue)
|   ElementValue(ElementValue_no_ElementValue) (COMMA ElementValue)+
;
ElementValue_no_ElementValue(SingleElementAnnotation) = Annotation(SingleElementAnnotation);
ElementValue_no_ElementValue_no_SingleElementAnnotation:
    ConditionalExpression
|   ElementValueArrayInitializer_no_ElementValue
|   Annotation_no_SingleElementAnnotation
;
ElementValue(SingleElementAnnotation):
    ElementValue_no_ElementValue(SingleElementAnnotation)
|   ElementValue_no_ElementValue(SingleElementAnnotation) ElementValue (ElementValue)+
;
ElementValue_no_SingleElementAnnotation:
    ElementValue_no_ElementValue_no_SingleElementAnnotation
|   ElementValue_no_ElementValue_no_SingleElementAnnotation ElementValue (ElementValue)+
;
ElementValueList(SingleElementAnnotation):
    ElementValue(SingleElementAnnotation)
|   ElementValue(SingleElementAnnotation) (COMMA ElementValue)+
;
ElementValueList_no_SingleElementAnnotation:
    ElementValue_no_SingleElementAnnotation
|   ElementValue_no_SingleElementAnnotation (COMMA ElementValue)+
;
Annotation(MarkerAnnotation) = MarkerAnnotation(MarkerAnnotation);
Annotation_no_MarkerAnnotation:
    NormalAnnotation
|   SingleElementAnnotation
;
ElementValue_no_ElementValue(MarkerAnnotation) = Annotation(MarkerAnnotation);
ElementValue_no_ElementValue_no_MarkerAnnotation:
    ConditionalExpression
|   ElementValueArrayInitializer_no_ElementValue
|   Annotation_no_MarkerAnnotation
;
ElementValue(MarkerAnnotation):
    ElementValue_no_ElementValue(MarkerAnnotation)
|   ElementValue_no_ElementValue(MarkerAnnotation) ElementValue (ElementValue)+
;
ElementValue_no_MarkerAnnotation:
    ElementValue_no_ElementValue_no_MarkerAnnotation
|   ElementValue_no_ElementValue_no_MarkerAnnotation ElementValue (ElementValue)+
;
ElementValueList(MarkerAnnotation):
    ElementValue(MarkerAnnotation)
|   ElementValue(MarkerAnnotation) (COMMA ElementValue)+
;
ElementValueList_no_MarkerAnnotation:
    ElementValue_no_MarkerAnnotation
|   ElementValue_no_MarkerAnnotation (COMMA ElementValue)+
;
ElementValue_no_ElementValue(ConditionalExpression) = ConditionalExpression(ConditionalExpression);
ElementValue_no_ElementValue_no_ConditionalExpression:
    ElementValueArrayInitializer_no_ElementValue
|   Annotation
;
ElementValue(ConditionalExpression):
    ElementValue_no_ElementValue(ConditionalExpression)
|   ElementValue_no_ElementValue(ConditionalExpression) ElementValue (ElementValue)+
;
ElementValue_no_ConditionalExpression:
    ElementValue_no_ElementValue_no_ConditionalExpression
|   ElementValue_no_ElementValue_no_ConditionalExpression ElementValue (ElementValue)+
;
ElementValueList(ConditionalExpression):
    ElementValue(ConditionalExpression)
|   ElementValue(ConditionalExpression) (COMMA ElementValue)+
;
ElementValueList_no_ConditionalExpression:
    ElementValue_no_ConditionalExpression
|   ElementValue_no_ConditionalExpression (COMMA ElementValue)+
;
ElementValue_no_ElementValue(Annotation) = Annotation(Annotation);
ElementValue_no_ElementValue_no_Annotation:
    ConditionalExpression
|   ElementValueArrayInitializer_no_ElementValue
;
ElementValue(Annotation):
    ElementValue_no_ElementValue(Annotation)
|   ElementValue_no_ElementValue(Annotation) ElementValue (ElementValue)+
;
ElementValue_no_Annotation:
    ElementValue_no_ElementValue_no_Annotation
|   ElementValue_no_ElementValue_no_Annotation ElementValue (ElementValue)+
;
ElementValueList(Annotation):
    ElementValue(Annotation)
|   ElementValue(Annotation) (COMMA ElementValue)+
;
ElementValueList_no_Annotation:
    ElementValue_no_Annotation
|   ElementValue_no_Annotation (COMMA ElementValue)+
;
ElementValue_no_ElementValue(NormalAnnotation) = Annotation(NormalAnnotation);
ElementValue_no_ElementValue_no_NormalAnnotation:
    ConditionalExpression
|   ElementValueArrayInitializer_no_ElementValue
|   Annotation_no_NormalAnnotation
;
ElementValue(NormalAnnotation):
    ElementValue_no_ElementValue(NormalAnnotation)
|   ElementValue_no_ElementValue(NormalAnnotation) ElementValue (ElementValue)+
;
ElementValue_no_NormalAnnotation:
    ElementValue_no_ElementValue_no_NormalAnnotation
|   ElementValue_no_ElementValue_no_NormalAnnotation ElementValue (ElementValue)+
;
ElementValueList(NormalAnnotation):
    ElementValue(NormalAnnotation)
|   ElementValue(NormalAnnotation) (COMMA ElementValue)+
;
ElementValueList_no_NormalAnnotation:
    ElementValue_no_NormalAnnotation
|   ElementValue_no_NormalAnnotation (COMMA ElementValue)+
;
ElementValue_no_ElementValue(ElementValueArrayInitializer_no_ElementValue) = ElementValueArrayInitializer_no_ElementValue(ElementValueArrayInitializer_no_ElementValue);
ElementValue_no_ElementValue_no_ElementValueArrayInitializer_no_ElementValue:
    ConditionalExpression
|   Annotation
;
ElementValue(ElementValueArrayInitializer_no_ElementValue):
    ElementValue_no_ElementValue(ElementValueArrayInitializer_no_ElementValue)
|   ElementValue_no_ElementValue(ElementValueArrayInitializer_no_ElementValue) ElementValue (ElementValue)+
;
ElementValue_no_ElementValueArrayInitializer_no_ElementValue:
    ElementValue_no_ElementValue_no_ElementValueArrayInitializer_no_ElementValue
|   ElementValue_no_ElementValue_no_ElementValueArrayInitializer_no_ElementValue ElementValue (ElementValue)+
;
ElementValueList(ElementValueArrayInitializer_no_ElementValue):
    ElementValue(ElementValueArrayInitializer_no_ElementValue)
|   ElementValue(ElementValueArrayInitializer_no_ElementValue) (COMMA ElementValue)+
;
ElementValueList_no_ElementValueArrayInitializer_no_ElementValue:
    ElementValue_no_ElementValueArrayInitializer_no_ElementValue
|   ElementValue_no_ElementValueArrayInitializer_no_ElementValue (COMMA ElementValue)+
;
ElementValueArrayInitializer_no_ElementValue(COMMA):
    COMMA(COMMA)
|   COMMA(COMMA) (COMMA | ElementValueList | ElementValueList COMMA)+
;
ElementValue_no_ElementValue(COMMA) = ElementValueArrayInitializer_no_ElementValue(COMMA);
ElementValue_no_ElementValue_no_COMMA:
    ConditionalExpression
|   Annotation
;
ElementValue(COMMA):
    ElementValue_no_ElementValue(COMMA)
|   ElementValue_no_ElementValue(COMMA) ElementValue (ElementValue)+
;
ElementValue_no_COMMA:
    ElementValue_no_ElementValue_no_COMMA
|   ElementValue_no_ElementValue_no_COMMA ElementValue (ElementValue)+
;
ElementValueList(COMMA):
    ElementValue(COMMA)
|   ElementValue(COMMA) (COMMA ElementValue)+
;
ElementValueList_no_COMMA:
    ElementValue_no_COMMA
|   ElementValue_no_COMMA (COMMA ElementValue)+
;
ConditionalExpression(Identifier) = Identifier(Identifier);
ElementValue_no_ElementValue(Identifier) = ConditionalExpression(Identifier);
ElementValue_no_ElementValue_no_Identifier:
    ElementValueArrayInitializer_no_ElementValue
|   Annotation
;
ElementValue(Identifier):
    ElementValue_no_ElementValue(Identifier)
|   ElementValue_no_ElementValue(Identifier) ElementValue (ElementValue)+
;
ElementValue_no_Identifier:
    ElementValue_no_ElementValue_no_Identifier
|   ElementValue_no_ElementValue_no_Identifier ElementValue (ElementValue)+
;
ElementValueList(Identifier):
    ElementValue(Identifier)
|   ElementValue(Identifier) (COMMA ElementValue)+
;
ElementValueList_no_Identifier:
    ElementValue_no_Identifier
|   ElementValue_no_Identifier (COMMA ElementValue)+
;
NormalAnnotation(AT):
    AT(AT) TypeName LPAREN RPAREN
|   AT(AT) TypeName LPAREN ElementValuePairList RPAREN
;
MarkerAnnotation(AT) = AT(AT) TypeName;
Annotation(AT):
    NormalAnnotation(AT)
|   MarkerAnnotation(AT)
|   SingleElementAnnotation(AT)
;
ElementValue_no_ElementValue(AT) = Annotation(AT);
ElementValue_no_ElementValue_no_AT:
    ConditionalExpression
|   ElementValueArrayInitializer_no_ElementValue
;
ElementValue(AT):
    ElementValue_no_ElementValue(AT)
|   ElementValue_no_ElementValue(AT) ElementValue (ElementValue)+
;
ElementValue_no_AT:
    ElementValue_no_ElementValue_no_AT
|   ElementValue_no_ElementValue_no_AT ElementValue (ElementValue)+
;
ElementValueList(AT):
    ElementValue(AT)
|   ElementValue(AT) (COMMA ElementValue)+
;
ElementValueList_no_AT:
    ElementValue_no_AT
|   ElementValue_no_AT (COMMA ElementValue)+
;
