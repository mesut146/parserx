include "lexer-jls.g"

TypeIdentifier = Identifier;
UnqualifiedMethodIdentifier:
    Identifier
|   VAR_KW
|   RECORD_KW
;
Literal:
    IntegerLiteral
|   FloatingPointLiteral
|   BooleanLiteral
|   CharacterLiteral
|   StringLiteral
|   TextBlock
|   NullLiteral
;
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
ArrayType:
    PrimitiveType Dims
|   ClassOrInterfaceType Dims
|   TypeVariable Dims
;
Dims:
    LBRACKET RBRACKET
|   LBRACKET RBRACKET (LBRACKET RBRACKET | Annotation+ LBRACKET RBRACKET)+
|   Annotation+ LBRACKET RBRACKET
|   Annotation+ LBRACKET RBRACKET (LBRACKET RBRACKET | Annotation+ LBRACKET RBRACKET)+
;
TypeParameter:
    TypeIdentifier
|   TypeIdentifier TypeBound
|   TypeParameterModifier+ TypeIdentifier
|   TypeParameterModifier+ TypeIdentifier TypeBound
;
TypeParameterModifier = Annotation;
TypeBound = EXTENDS_KEYWORD (TypeVariable | ClassOrInterfaceType AdditionalBound*);
AdditionalBound = AND InterfaceType;
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
CompilationUnit:
    OrdinaryCompilationUnit
|   ModularCompilationUnit
;
OrdinaryCompilationUnit:
    TopLevelClassOrInterfaceDeclaration+
|   ImportDeclaration+
|   ImportDeclaration+ TopLevelClassOrInterfaceDeclaration+
|   PackageDeclaration
|   PackageDeclaration TopLevelClassOrInterfaceDeclaration+
|   PackageDeclaration ImportDeclaration+
|   PackageDeclaration ImportDeclaration+ TopLevelClassOrInterfaceDeclaration+
;
ModularCompilationUnit:
    ModuleDeclaration
|   ImportDeclaration+ ModuleDeclaration
;
PackageDeclaration:
    PACKAGE_KEYWORD Identifier
|   PACKAGE_KEYWORD Identifier (DOT Identifier)+
|   PackageModifier+ PACKAGE_KEYWORD Identifier
|   PackageModifier+ PACKAGE_KEYWORD Identifier (DOT Identifier)+
;
PackageModifier = Annotation;
ImportDeclaration:
    SingleTypeImportDeclaration
|   TypeImportOnDemandDeclaration
|   SingleStaticImportDeclaration
|   StaticImportOnDemandDeclaration
;
SingleTypeImportDeclaration = IMPORT_KEYWORD TypeName;
TypeImportOnDemandDeclaration = IMPORT_KEYWORD PackageOrTypeName DOT STAR;
SingleStaticImportDeclaration = IMPORT_KEYWORD STATIC_KEYWORD TypeName DOT Identifier;
StaticImportOnDemandDeclaration = IMPORT_KEYWORD STATIC_KEYWORD TypeName DOT STAR;
TopLevelClassOrInterfaceDeclaration:
    ClassDeclaration
|   InterfaceDeclaration
;
ModuleName:
    ModuleName_no_ModuleName
|   ModuleName_no_ModuleName ModuleName(ModuleName)+
;
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
ExpressionName:
    Identifier
|   AmbiguousName DOT Identifier
;
MethodName = UnqualifiedMethodIdentifier;
AmbiguousName:
    AmbiguousName_no_AmbiguousName
|   AmbiguousName_no_AmbiguousName AmbiguousName(AmbiguousName)+
;
ModuleDeclaration:
    MODULE_KW Identifier
|   MODULE_KW Identifier (DOT Identifier)+
|   OPEN_KEYWORD MODULE_KW Identifier
|   OPEN_KEYWORD MODULE_KW Identifier (DOT Identifier)+
|   Annotation+ MODULE_KW Identifier
|   Annotation+ MODULE_KW Identifier (DOT Identifier)+
|   Annotation+ OPEN_KEYWORD MODULE_KW Identifier
|   Annotation+ OPEN_KEYWORD MODULE_KW Identifier (DOT Identifier)+
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
ModuleDirective:
    REQUIRES_KW ModuleName
|   REQUIRES_KW RequiresModifier+ ModuleName
|   EXPORTS_KW PackageName
|   EXPORTS_KW PackageName (TO_KW ModuleName | TO_KW ModuleName (COMMA ModuleName)+)
|   OPENS_KW PackageName
|   OPENS_KW PackageName (TO_KW ModuleName | TO_KW ModuleName (COMMA ModuleName)+)
|   USES TypeName
|   PROVIDES_KW TypeName WITH_KW TypeName
|   PROVIDES_KW TypeName WITH_KW TypeName (COMMA TypeName)+
;
RequiresModifier:
    TRANSITIVE_KW
|   STATIC_KEYWORD
;
ClassDeclaration:
    NormalClassDeclaration
|   EnumDeclaration
|   RecordDeclaration
;
NormalClassDeclaration:
    CLASS_KEYWORD TypeIdentifier ClassBody
|   CLASS_KEYWORD TypeIdentifier ClassImplements ClassBody
|   CLASS_KEYWORD TypeIdentifier ClassExtends ClassBody
|   CLASS_KEYWORD TypeIdentifier ClassExtends ClassImplements ClassBody
|   CLASS_KEYWORD TypeIdentifier TypeParameters ClassBody
|   CLASS_KEYWORD TypeIdentifier TypeParameters ClassImplements ClassBody
|   CLASS_KEYWORD TypeIdentifier TypeParameters ClassExtends ClassBody
|   CLASS_KEYWORD TypeIdentifier TypeParameters ClassExtends ClassImplements ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier ClassImplements ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier ClassExtends ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier ClassExtends ClassImplements ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier TypeParameters ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier TypeParameters ClassImplements ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier TypeParameters ClassExtends ClassBody
|   ClassModifier+ CLASS_KEYWORD TypeIdentifier TypeParameters ClassExtends ClassImplements ClassBody
;
ClassModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PROTECTED_KEYWORD
|   PRIVATE_KEYWORD ABSTRACT_KEYWORD
|   STATIC_KEYWORD
|   FINAL_KEYWORD
|   STRICTFP_KEYWORD
;
ConstructorDeclaration:
    ConstructorDeclarator ConstructorBody
|   ConstructorDeclarator Throws ConstructorBody
|   ConstructorModifier+ ConstructorDeclarator ConstructorBody
|   ConstructorModifier+ ConstructorDeclarator Throws ConstructorBody
;
ConstructorDeclarator:
    SimpleTypeName LPAREN RPAREN
|   SimpleTypeName LPAREN FormalParameterList RPAREN
|   SimpleTypeName LPAREN ReceiverParameter COMMA RPAREN
|   SimpleTypeName LPAREN ReceiverParameter COMMA FormalParameterList RPAREN
|   TypeParameters SimpleTypeName LPAREN RPAREN
|   TypeParameters SimpleTypeName LPAREN FormalParameterList RPAREN
|   TypeParameters SimpleTypeName LPAREN ReceiverParameter COMMA RPAREN
|   TypeParameters SimpleTypeName LPAREN ReceiverParameter COMMA FormalParameterList RPAREN
;
SimpleTypeName = TypeIdentifier;
ConstructorModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PROTECTED_KEYWORD
|   PRIVATE_KEYWORD
;
ConstructorBody:
    LBRACE RBRACE
|   LBRACE BlockStatements RBRACE
|   LBRACE ExplicitConstructorInvocation RBRACE
|   LBRACE ExplicitConstructorInvocation BlockStatements RBRACE
;
ExplicitConstructorInvocation:
    THIS_KEYWORD LPAREN RPAREN SEMI
|   THIS_KEYWORD LPAREN ArgumentList RPAREN SEMI
|   TypeArguments THIS_KEYWORD LPAREN RPAREN SEMI
|   TypeArguments THIS_KEYWORD LPAREN ArgumentList RPAREN SEMI
|   SUPER_KEYWORD LPAREN RPAREN SEMI
|   SUPER_KEYWORD LPAREN ArgumentList RPAREN SEMI
|   TypeArguments SUPER_KEYWORD LPAREN RPAREN SEMI
|   TypeArguments SUPER_KEYWORD LPAREN ArgumentList RPAREN SEMI
|   ExpressionName DOT SUPER_KEYWORD LPAREN RPAREN SEMI
|   ExpressionName DOT SUPER_KEYWORD LPAREN ArgumentList RPAREN SEMI
|   ExpressionName DOT TypeArguments SUPER_KEYWORD LPAREN RPAREN SEMI
|   ExpressionName DOT TypeArguments SUPER_KEYWORD LPAREN ArgumentList RPAREN SEMI
|   Primary DOT SUPER_KEYWORD LPAREN RPAREN SEMI
|   Primary DOT SUPER_KEYWORD LPAREN ArgumentList RPAREN SEMI
|   Primary DOT TypeArguments SUPER_KEYWORD LPAREN RPAREN SEMI
|   Primary DOT TypeArguments SUPER_KEYWORD LPAREN ArgumentList RPAREN SEMI
;
EnumDeclaration:
    ENUM_KEYWORD TypeIdentifier EnumBody
|   ENUM_KEYWORD TypeIdentifier ClassImplements EnumBody
|   ClassModifier+ ENUM_KEYWORD TypeIdentifier EnumBody
|   ClassModifier+ ENUM_KEYWORD TypeIdentifier ClassImplements EnumBody
;
EnumBody:
    LBRACE RBRACE
|   LBRACE EnumBodyDeclarations RBRACE
|   LBRACE COMMA RBRACE
|   LBRACE COMMA EnumBodyDeclarations RBRACE
|   LBRACE EnumConstantList RBRACE
|   LBRACE EnumConstantList EnumBodyDeclarations RBRACE
|   LBRACE EnumConstantList COMMA RBRACE
|   LBRACE EnumConstantList COMMA EnumBodyDeclarations RBRACE
;
EnumConstantList:
    EnumConstant
|   EnumConstant (COMMA EnumConstant)+
;
EnumConstant:
    Identifier
|   Identifier ClassBody
|   Identifier (LPAREN RPAREN | LPAREN ArgumentList RPAREN)
|   Identifier (LPAREN RPAREN | LPAREN ArgumentList RPAREN) ClassBody
|   EnumConstantModifier+ Identifier
|   EnumConstantModifier+ Identifier ClassBody
|   EnumConstantModifier+ Identifier (LPAREN RPAREN | LPAREN ArgumentList RPAREN)
|   EnumConstantModifier+ Identifier (LPAREN RPAREN | LPAREN ArgumentList RPAREN) ClassBody
;
EnumConstantModifier = Annotation;
EnumBodyDeclarations:
    SEMI
|   SEMI ClassBodyDeclaration+
;
RecordDeclaration:
    RECORD_KW TypeIdentifier RecordHeader RecordBody
|   RECORD_KW TypeIdentifier RecordHeader ClassImplements RecordBody
|   RECORD_KW TypeIdentifier TypeParameters RecordHeader RecordBody
|   RECORD_KW TypeIdentifier TypeParameters RecordHeader ClassImplements RecordBody
|   ClassModifier+ RECORD_KW TypeIdentifier RecordHeader RecordBody
|   ClassModifier+ RECORD_KW TypeIdentifier RecordHeader ClassImplements RecordBody
|   ClassModifier+ RECORD_KW TypeIdentifier TypeParameters RecordHeader RecordBody
|   ClassModifier+ RECORD_KW TypeIdentifier TypeParameters RecordHeader ClassImplements RecordBody
;
RecordHeader:
    LPAREN RPAREN
|   LPAREN RecordComponentList RPAREN
;
RecordComponentList:
    RecordComponent
|   RecordComponent (COMMA RecordComponent)+
;
RecordComponent:
    UnannType Identifier
|   RecordComponentModifier+ UnannType Identifier
|   VariableArityRecordComponent
;
VariableArityRecordComponent:
    UnannType VARARGS Identifier
|   UnannType Annotation+ VARARGS Identifier
|   RecordComponentModifier+ UnannType VARARGS Identifier
|   RecordComponentModifier+ UnannType Annotation+ VARARGS Identifier
;
RecordComponentModifier = Annotation;
RecordBody:
    LBRACE RBRACE
|   LBRACE RecordBodyDeclaration+ RBRACE
;
RecordBodyDeclaration:
    ClassBodyDeclaration
|   CompactConstructorDeclaration
;
CompactConstructorDeclaration:
    SimpleTypeName ConstructorBody
|   ConstructorModifier+ SimpleTypeName ConstructorBody
;
TypeParameters = LT TypeParameterList GT;
TypeParameterList:
    TypeParameter
|   TypeParameter (COMMA TypeParameter)+
;
ClassExtends = EXTENDS_KEYWORD ClassType;
ClassImplements = IMPLEMENTS_KEYWORD InterfaceTypeList;
InterfaceTypeList:
    InterfaceType
|   InterfaceType (COMMA InterfaceType)+
;
ClassBody:
    LBRACE RBRACE
|   LBRACE ClassBodyDeclaration+ RBRACE
;
ClassBodyDeclaration:
    ClassMemberDeclaration
|   InstanceInitializer
|   StaticInitializer
|   ConstructorDeclaration
;
InstanceInitializer = Block;
StaticInitializer = STATIC_KEYWORD Block;
ClassMemberDeclaration:
    FieldDeclaration
|   MethodDeclaration
|   ClassDeclaration
|   InterfaceDeclaration
;
InterfaceDeclaration:
    NormalInterfaceDeclaration
|   AnnotationInterfaceDeclaration
;
NormalInterfaceDeclaration:
    INTERFACE_KEYWORD TypeIdentifier InterfaceBody
|   INTERFACE_KEYWORD TypeIdentifier InterfaceExtends InterfaceBody
|   INTERFACE_KEYWORD TypeIdentifier TypeParameters InterfaceBody
|   INTERFACE_KEYWORD TypeIdentifier TypeParameters InterfaceExtends InterfaceBody
|   InterfaceModifier+ INTERFACE_KEYWORD TypeIdentifier InterfaceBody
|   InterfaceModifier+ INTERFACE_KEYWORD TypeIdentifier InterfaceExtends InterfaceBody
|   InterfaceModifier+ INTERFACE_KEYWORD TypeIdentifier TypeParameters InterfaceBody
|   InterfaceModifier+ INTERFACE_KEYWORD TypeIdentifier TypeParameters InterfaceExtends InterfaceBody
;
InterfaceModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PROTECTED_KEYWORD
|   PRIVATE_KEYWORD
|   ABSTRACT_KEYWORD
|   STATIC_KEYWORD
|   STRICTFP_KEYWORD
;
InterfaceExtends = EXTENDS_KEYWORD InterfaceTypeList;
InterfaceBody:
    LBRACE RBRACE
|   LBRACE InterfaceMemberDeclaration+ RBRACE
;
InterfaceMemberDeclaration:
    ConstantDeclaration
|   InterfaceMethodDeclaration
|   ClassDeclaration
|   InterfaceDeclaration
|   SEMI
;
ConstantDeclaration:
    UnannType VariableDeclaratorList SEMI
|   ConstantModifier+ UnannType VariableDeclaratorList SEMI
;
ConstantModifier:
    Annotation
|   PUBLIC_KEYWORD
|   STATIC_KEYWORD
|   FINAL_KEYWORD
;
InterfaceMethodDeclaration:
    MethodHeader MethodBody
|   InterfaceMethodModifier+ MethodHeader MethodBody
;
InterfaceMethodModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PRIVATE_KEYWORD
|   ABSTRACT_KEYWORD
|   DEFAULT_KEYWORD
|   STATIC_KEYWORD
|   STRICTFP_KEYWORD
;
AnnotationInterfaceDeclaration:
    AT INTERFACE_KEYWORD TypeIdentifier AnnotationInterfaceBody
|   InterfaceModifier+ AT INTERFACE_KEYWORD TypeIdentifier AnnotationInterfaceBody
;
AnnotationInterfaceBody:
    LBRACE RBRACE
|   LBRACE AnnotationInterfaceMemberDeclaration+ RBRACE
;
AnnotationInterfaceMemberDeclaration:
    AnnotationInterfaceElementDeclaration
|   ConstantDeclaration
|   ClassDeclaration
|   InterfaceDeclaration
|   SEMI
;
AnnotationInterfaceElementDeclaration:
    UnannType Identifier LPAREN RPAREN SEMI
|   UnannType Identifier LPAREN RPAREN DefaultValue SEMI
|   UnannType Identifier LPAREN RPAREN Dims SEMI
|   UnannType Identifier LPAREN RPAREN Dims DefaultValue SEMI
|   AnnotationInterfaceElementModifier+ UnannType Identifier LPAREN RPAREN SEMI
|   AnnotationInterfaceElementModifier+ UnannType Identifier LPAREN RPAREN DefaultValue SEMI
|   AnnotationInterfaceElementModifier+ UnannType Identifier LPAREN RPAREN Dims SEMI
|   AnnotationInterfaceElementModifier+ UnannType Identifier LPAREN RPAREN Dims DefaultValue SEMI
;
AnnotationInterfaceElementModifier:
    Annotation
|   PUBLIC_KEYWORD
|   ABSTRACT_KEYWORD
;
DefaultValue = DEFAULT_KEYWORD ElementValue;
ArrayInitializer:
    LBRACE RBRACE
|   LBRACE COMMA RBRACE
|   LBRACE VariableInitializerList RBRACE
|   LBRACE VariableInitializerList COMMA RBRACE
;
VariableInitializerList:
    VariableInitializer
|   VariableInitializer (COMMA VariableInitializer)+
;
FieldDeclaration:
    UnannType VariableDeclaratorList
|   FieldModifier+ UnannType VariableDeclaratorList
;
VariableDeclaratorList:
    VariableDeclarator
|   VariableDeclarator (COMMA VariableDeclarator)+
;
VariableDeclarator:
    VariableDeclaratorId
|   VariableDeclaratorId EQ VariableInitializer
;
VariableDeclaratorId:
    Identifier
|   Identifier Dims
;
VariableInitializer:
    Expression
|   ArrayInitializer
;
UnannType:
    UnannPrimitiveType
|   UnannReferenceType
;
UnannPrimitiveType:
    NumericType
|   BOOLEAN_KEYWORD
;
UnannReferenceType:
    UnannClassOrInterfaceType
|   UnannTypeVariable
|   UnannArrayType
;
UnannClassOrInterfaceType:
    UnannClassOrInterfaceType_no_UnannClassOrInterfaceType
|   UnannClassOrInterfaceType_no_UnannClassOrInterfaceType UnannClassOrInterfaceType(UnannClassOrInterfaceType)+
;
UnannClassType:
    TypeIdentifier
|   TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
|   UnannClassOrInterfaceType DOT TypeIdentifier
|   UnannClassOrInterfaceType DOT TypeIdentifier TypeArguments
|   UnannClassOrInterfaceType DOT Annotation+ TypeIdentifier
|   UnannClassOrInterfaceType DOT Annotation+ TypeIdentifier TypeArguments
;
UnannInterfaceType = UnannClassType;
UnannTypeVariable = TypeIdentifier;
UnannArrayType:
    UnannPrimitiveType Dims
|   UnannClassOrInterfaceType Dims
|   UnannTypeVariable Dims
;
FieldModifier:
    Annotation
|   PUBLIC_KEYWORD PROTECTED_KEYWORD
|   PRIVATE_KEYWORD
|   STATIC_KEYWORD
|   FINAL_KEYWORD
|   TRANSIENT_KEYWORD
|   VOLATILE_KEYWORD
;
MethodDeclaration:
    MethodHeader MethodBody
|   MethodModifier+ MethodHeader MethodBody
;
MethodHeader:
    Result MethodDeclarator
|   Result MethodDeclarator Throws
|   TypeParameters Result MethodDeclarator
|   TypeParameters Result MethodDeclarator Throws
|   TypeParameters Annotation+ Result MethodDeclarator
|   TypeParameters Annotation+ Result MethodDeclarator Throws
;
MethodDeclarator:
    Identifier LPAREN RPAREN
|   Identifier LPAREN RPAREN Dims
|   Identifier LPAREN FormalParameterList RPAREN
|   Identifier LPAREN FormalParameterList RPAREN Dims
|   Identifier LPAREN ReceiverParameter COMMA RPAREN
|   Identifier LPAREN ReceiverParameter COMMA RPAREN Dims
|   Identifier LPAREN ReceiverParameter COMMA FormalParameterList RPAREN
|   Identifier LPAREN ReceiverParameter COMMA FormalParameterList RPAREN Dims
;
ReceiverParameter:
    UnannType THIS_KEYWORD
|   UnannType Identifier DOT THIS_KEYWORD
|   Annotation+ UnannType THIS_KEYWORD
|   Annotation+ UnannType Identifier DOT THIS_KEYWORD
;
FormalParameterList:
    FormalParameter
|   FormalParameter (COMMA FormalParameter)+
;
FormalParameter:
    UnannType VariableDeclaratorId
|   VariableModifier+ UnannType VariableDeclaratorId
|   VariableArityParameter
;
VariableArityParameter:
    UnannType VARARGS Identifier
|   UnannType Annotation+ VARARGS Identifier
|   VariableModifier+ UnannType VARARGS Identifier
|   VariableModifier+ UnannType Annotation+ VARARGS Identifier
;
VariableModifier:
    Annotation
|   FINAL_KEYWORD
;
MethodModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PROTECTED_KEYWORD
|   PRIVATE_KEYWORD
|   ABSTRACT_KEYWORD
|   STATIC_KEYWORD
|   FINAL_KEYWORD
|   SYNCHRONIZED_KEYWORD
|   NATIVE_KEYWORD
|   STRICTFP_KEYWORD
;
Result:
    UnannType
|   VOID_KEYWORD
;
Throws = THROWS_KEYWORD ExceptionTypeList;
ExceptionTypeList:
    ExceptionType
|   ExceptionType (COMMA ExceptionType)+
;
ExceptionType:
    ClassType
|   TypeVariable
;
MethodBody = Block;
Block:
    LBRACE RBRACE
|   LBRACE BlockStatements RBRACE
;
BlockStatements:
    BlockStatement
|   BlockStatement BlockStatement+
;
BlockStatement:
    LocalClassOrInterfaceDeclaration
|   LocalVariableDeclarationStatement
|   Statement
;
LocalClassOrInterfaceDeclaration:
    ClassDeclaration
|   NormalInterfaceDeclaration
;
LocalVariableDeclaration:
    LocalVariableType VariableDeclaratorList
|   VariableModifier+ LocalVariableType VariableDeclaratorList
;
LocalVariableType:
    UnannType
|   VAR_KW
;
LocalVariableDeclarationStatement = LocalVariableDeclaration SEMI;
Statement:
    StatementWithoutTrailingSubstatement
|   LabeledStatement
|   IfThenStatement
|   IfThenElseStatement
|   WhileStatement
|   ForStatement
;
StatementNoShortIf:
    StatementWithoutTrailingSubstatement
|   LabeledStatementNoShortIf
|   IfThenElseStatementNoShortIf
|   WhileStatementNoShortIf
|   ForStatementNoShortIf
;
StatementWithoutTrailingSubstatement:
    Block
|   EmptyStatement
|   ExpressionStatement
|   AssertStatement
|   SwitchStatement
|   DoStatement
|   BreakStatement
|   ContinueStatement
|   ReturnStatement
|   SynchronizedStatement
|   ThrowStatement
|   TryStatement
|   YieldStatement
;
EmptyStatement = SEMI;
LabeledStatement = Identifier COLON Statement;
LabeledStatementNoShortIf = Identifier COLON StatementNoShortIf;
ExpressionStatement = StatementExpression SEMI;
StatementExpression:
    Assignment
|   PreIncrementExpression
|   PreDecrementExpression
|   PostIncrementExpression
|   PostDecrementExpression
|   MethodInvocation
|   ClassInstanceCreationExpression
;
IfThenStatement = IF_KEYWORD LPAREN Expression RPAREN Statement;
IfThenElseStatement = IF_KEYWORD LPAREN Expression RPAREN StatementNoShortIf ELSE_KEYWORD Statement;
IfThenElseStatementNoShortIf = IF_KEYWORD LPAREN Expression RPAREN StatementNoShortIf ELSE_KEYWORD StatementNoShortIf;
AssertStatement:
    ASSERT_KEYWORD Expression SEMI
|   ASSERT_KEYWORD Expression COLON Expression SEMI
;
SwitchStatement = SWITCH_KEYWORD LPAREN Expression RPAREN SwitchBlock;
SwitchBlock:
    LBRACE SwitchRule RBRACE
|   LBRACE SwitchRule SwitchRule+ RBRACE
|   LBRACE RBRACE
|   LBRACE (SwitchLabel COLON)+ RBRACE
|   LBRACE SwitchBlockStatementGroup+ RBRACE
|   LBRACE SwitchBlockStatementGroup+ (SwitchLabel COLON)+ RBRACE
;
SwitchRule = SwitchLabel ARROW (Expression SEMI | Block | ThrowStatement);
SwitchBlockStatementGroup:
    SwitchLabel COLON BlockStatements
|   SwitchLabel COLON (SwitchLabel COLON)+ BlockStatements
;
SwitchLabel:
    CASE_KEYWORD CaseConstant
|   CASE_KEYWORD CaseConstant (COMMA CaseConstant)+
|   DEFAULT_KEYWORD
;
CaseConstant = ConditionalExpression;
WhileStatement = WHILE_KEYWORD LPAREN Expression RPAREN Statement;
WhileStatementNoShortIf = WHILE_KEYWORD LPAREN Expression RPAREN StatementNoShortIf;
DoStatement = DO_KEYWORD Statement WHILE_KEYWORD LPAREN Expression RPAREN SEMI;
ForStatement:
    BasicForStatement
|   EnhancedForStatement
;
ForStatementNoShortIf:
    BasicForStatementNoShortIf
|   EnhancedForStatementNoShortIf
;
BasicForStatement:
    FOR_KEYWORD LPAREN SEMI SEMI RPAREN Statement
|   FOR_KEYWORD LPAREN SEMI SEMI ForUpdate RPAREN Statement
|   FOR_KEYWORD LPAREN SEMI Expression SEMI RPAREN Statement
|   FOR_KEYWORD LPAREN SEMI Expression SEMI ForUpdate RPAREN Statement
|   FOR_KEYWORD LPAREN ForInit SEMI SEMI RPAREN Statement
|   FOR_KEYWORD LPAREN ForInit SEMI SEMI ForUpdate RPAREN Statement
|   FOR_KEYWORD LPAREN ForInit SEMI Expression SEMI RPAREN Statement
|   FOR_KEYWORD LPAREN ForInit SEMI Expression SEMI ForUpdate RPAREN Statement
;
BasicForStatementNoShortIf:
    FOR_KEYWORD LPAREN SEMI SEMI RPAREN StatementNoShortIf
|   FOR_KEYWORD LPAREN SEMI SEMI ForUpdate RPAREN StatementNoShortIf
|   FOR_KEYWORD LPAREN SEMI Expression SEMI RPAREN StatementNoShortIf
|   FOR_KEYWORD LPAREN SEMI Expression SEMI ForUpdate RPAREN StatementNoShortIf
|   FOR_KEYWORD LPAREN ForInit SEMI SEMI RPAREN StatementNoShortIf
|   FOR_KEYWORD LPAREN ForInit SEMI SEMI ForUpdate RPAREN StatementNoShortIf
|   FOR_KEYWORD LPAREN ForInit SEMI Expression SEMI RPAREN StatementNoShortIf
|   FOR_KEYWORD LPAREN ForInit SEMI Expression SEMI ForUpdate RPAREN StatementNoShortIf
;
ForInit:
    StatementExpressionList
|   LocalVariableDeclaration
;
ForUpdate = StatementExpressionList;
StatementExpressionList:
    StatementExpression
|   StatementExpression (COMMA StatementExpression)+
;
EnhancedForStatement = FOR_KEYWORD LPAREN LocalVariableDeclaration COLON Expression RPAREN Statement;
EnhancedForStatementNoShortIf = FOR_KEYWORD LPAREN LocalVariableDeclaration COLON Expression RPAREN StatementNoShortIf;
BreakStatement:
    BREAK_KEYWORD SEMI
|   BREAK_KEYWORD Identifier SEMI
;
ContinueStatement:
    CONTINUE_KEYWORD SEMI
|   CONTINUE_KEYWORD Identifier SEMI
;
ReturnStatement:
    RETURN_KEYWORD SEMI
|   RETURN_KEYWORD Expression SEMI
;
ThrowStatement = THROW_KEYWORD Expression SEMI;
SynchronizedStatement = SYNCHRONIZED_KEYWORD LPAREN Expression RPAREN Block;
TryStatement:
    TRY_KEYWORD Block (Catches Finally? | Finally)
|   TryWithResourcesStatement
;
Catches:
    CatchClause
|   CatchClause CatchClause+
;
CatchClause = CATCH_KEYWORD LPAREN CatchFormalParameter RPAREN Block;
CatchFormalParameter:
    CatchType VariableDeclaratorId
|   VariableModifier+ CatchType VariableDeclaratorId
;
CatchType:
    UnannClassType
|   UnannClassType (OR ClassType)+
;
Finally = FINALLY_KEYWORD Block;
TryWithResourcesStatement:
    TRY_KEYWORD ResourceSpecification Block
|   TRY_KEYWORD ResourceSpecification Block Finally
|   TRY_KEYWORD ResourceSpecification Block Catches
|   TRY_KEYWORD ResourceSpecification Block Catches Finally
;
ResourceSpecification:
    LPAREN ResourceList RPAREN
|   LPAREN ResourceList SEMI RPAREN
;
ResourceList:
    Resource
|   Resource (SEMI Resource)+
;
Resource:
    LocalVariableDeclaration
|   VariableAccess
;
VariableAccess:
    ExpressionName
|   FieldAccess
;
YieldStatement = YIELD_KW Expression SEMI;
Expression:
    LambdaExpression
|   AssignmentExpression
;
Primary:
    Primary_no_Primary
|   Primary_no_Primary Primary(Primary)+
;
PrimaryNoNewArray:
    PrimaryNoNewArray_no_PrimaryNoNewArray
|   PrimaryNoNewArray_no_PrimaryNoNewArray PrimaryNoNewArray(PrimaryNoNewArray)+
;
ClassLiteral:
    TypeName DOT CLASS_KEYWORD
|   TypeName (LBRACKET RBRACKET)+ DOT CLASS_KEYWORD
|   NumericType DOT CLASS_KEYWORD
|   NumericType (LBRACKET RBRACKET)+ DOT CLASS_KEYWORD
|   BOOLEAN_KEYWORD DOT CLASS_KEYWORD
|   BOOLEAN_KEYWORD (LBRACKET RBRACKET)+ DOT CLASS_KEYWORD
|   VOID_KEYWORD DOT CLASS_KEYWORD
;
ClassInstanceCreationExpression:
    UnqualifiedClassInstanceCreationExpression
|   ExpressionName DOT UnqualifiedClassInstanceCreationExpression
|   Primary DOT UnqualifiedClassInstanceCreationExpression
;
UnqualifiedClassInstanceCreationExpression:
    NEW_KEYWORD ClassOrInterfaceTypeToInstantiate LPAREN RPAREN
|   NEW_KEYWORD ClassOrInterfaceTypeToInstantiate LPAREN RPAREN ClassBody
|   NEW_KEYWORD ClassOrInterfaceTypeToInstantiate LPAREN ArgumentList RPAREN
|   NEW_KEYWORD ClassOrInterfaceTypeToInstantiate LPAREN ArgumentList RPAREN ClassBody
|   NEW_KEYWORD TypeArguments ClassOrInterfaceTypeToInstantiate LPAREN RPAREN
|   NEW_KEYWORD TypeArguments ClassOrInterfaceTypeToInstantiate LPAREN RPAREN ClassBody
|   NEW_KEYWORD TypeArguments ClassOrInterfaceTypeToInstantiate LPAREN ArgumentList RPAREN
|   NEW_KEYWORD TypeArguments ClassOrInterfaceTypeToInstantiate LPAREN ArgumentList RPAREN ClassBody
;
ClassOrInterfaceTypeToInstantiate:
    Identifier
|   Identifier TypeArgumentsOrDiamond
|   Identifier (DOT Identifier | DOT Annotation+ Identifier)+
|   Identifier (DOT Identifier | DOT Annotation+ Identifier)+ TypeArgumentsOrDiamond
|   Annotation+ Identifier
|   Annotation+ Identifier TypeArgumentsOrDiamond
|   Annotation+ Identifier (DOT Identifier | DOT Annotation+ Identifier)+
|   Annotation+ Identifier (DOT Identifier | DOT Annotation+ Identifier)+ TypeArgumentsOrDiamond
;
TypeArgumentsOrDiamond:
    TypeArguments
|   LT GT
;
ArrayCreationExpression:
    NEW_KEYWORD (PrimitiveType | ClassOrInterfaceType)
|   NEW_KEYWORD (PrimitiveType | ClassOrInterfaceType) (DimExprs | DimExprs Dims | Dims ArrayInitializer)
;
DimExprs:
    DimExpr
|   DimExpr DimExpr+
;
DimExpr:
    Expression
|   Annotation+
|   Annotation+ Expression
;
ArrayAccess:
    ExpressionName
|   ExpressionName Expression
|   PrimaryNoNewArray
|   PrimaryNoNewArray Expression
;
FieldAccess = (Primary | SUPER_KEYWORD | TypeName DOT SUPER_KEYWORD) DOT Identifier;
MethodInvocation:
    MethodName LPAREN RPAREN
|   MethodName LPAREN ArgumentList RPAREN
|   TypeName DOT Identifier LPAREN RPAREN
|   TypeName DOT Identifier LPAREN ArgumentList RPAREN
|   TypeName DOT TypeArguments Identifier LPAREN RPAREN
|   TypeName DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   ExpressionName DOT Identifier LPAREN RPAREN
|   ExpressionName DOT Identifier LPAREN ArgumentList RPAREN
|   ExpressionName DOT TypeArguments Identifier LPAREN RPAREN
|   ExpressionName DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   Primary DOT Identifier LPAREN RPAREN
|   Primary DOT Identifier LPAREN ArgumentList RPAREN
|   Primary DOT TypeArguments Identifier LPAREN RPAREN
|   Primary DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   SUPER_KEYWORD DOT Identifier RPAREN
|   SUPER_KEYWORD DOT Identifier ArgumentList RPAREN
|   SUPER_KEYWORD DOT Identifier LPAREN RPAREN
|   SUPER_KEYWORD DOT Identifier LPAREN ArgumentList RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier ArgumentList RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier LPAREN RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   TypeName DOT SUPER_KEYWORD DOT Identifier LPAREN RPAREN
|   TypeName DOT SUPER_KEYWORD DOT Identifier LPAREN ArgumentList RPAREN
|   TypeName DOT SUPER_KEYWORD DOT TypeArguments Identifier LPAREN RPAREN
|   TypeName DOT SUPER_KEYWORD DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
;
ArgumentList:
    Expression
|   Expression (COMMA Expression)+
;
MethodReference:
    ExpressionName COLONCOLON Identifier
|   ExpressionName COLONCOLON TypeArguments Identifier
|   Primary COLONCOLON Identifier
|   Primary COLONCOLON TypeArguments Identifier
|   ReferenceType COLONCOLON Identifier
|   ReferenceType COLONCOLON TypeArguments Identifier
|   SUPER_KEYWORD COLONCOLON Identifier
|   SUPER_KEYWORD COLONCOLON TypeArguments Identifier
|   TypeName DOT SUPER_KEYWORD COLONCOLON Identifier
|   TypeName DOT SUPER_KEYWORD COLONCOLON TypeArguments Identifier
|   ClassType COLONCOLON NEW_KEYWORD
|   ClassType COLONCOLON TypeArguments NEW_KEYWORD
|   ArrayType COLONCOLON NEW_KEYWORD
;
PostfixExpression:
    PostfixExpression_no_PostfixExpression
|   PostfixExpression_no_PostfixExpression PostfixExpression(PostfixExpression)+
;
PostIncrementExpression = PostfixExpression PLUSPLUS;
PostDecrementExpression = PostfixExpression MINUSMINUS;
UnaryExpression:
    PreIncrementExpression
|   PreDecrementExpression
|   PLUS UnaryExpression
|   MINUS UnaryExpression
|   UnaryExpressionNotPlusMinus
;
PreIncrementExpression = PLUSPLUS UnaryExpression;
PreDecrementExpression = MINUSMINUS UnaryExpression;
UnaryExpressionNotPlusMinus:
    PostfixExpression
|   TILDE UnaryExpression
|   BANG UnaryExpression
|   CastExpression
|   SwitchExpression
;
CastExpression:
    LPAREN PrimitiveType RPAREN UnaryExpression
|   LPAREN ReferenceType RPAREN UnaryExpressionNotPlusMinus
|   LPAREN ReferenceType AdditionalBound+ RPAREN UnaryExpressionNotPlusMinus
|   LPAREN ReferenceType RPAREN LambdaExpression
|   LPAREN ReferenceType AdditionalBound+ RPAREN LambdaExpression
;
MultiplicativeExpression:
    MultiplicativeExpression_no_MultiplicativeExpression
|   MultiplicativeExpression_no_MultiplicativeExpression MultiplicativeExpression(MultiplicativeExpression)+
;
AdditiveExpression:
    AdditiveExpression_no_AdditiveExpression
|   AdditiveExpression_no_AdditiveExpression AdditiveExpression(AdditiveExpression)+
;
ShiftExpression:
    ShiftExpression_no_ShiftExpression
|   ShiftExpression_no_ShiftExpression ShiftExpression(ShiftExpression)+
;
RelationalExpression:
    RelationalExpression_no_RelationalExpression
|   RelationalExpression_no_RelationalExpression RelationalExpression(RelationalExpression)+
;
EqualityExpression:
    EqualityExpression_no_EqualityExpression
|   EqualityExpression_no_EqualityExpression EqualityExpression(EqualityExpression)+
;
AndExpression:
    AndExpression_no_AndExpression
|   AndExpression_no_AndExpression AndExpression(AndExpression)+
;
ExclusiveOrExpression:
    ExclusiveOrExpression_no_ExclusiveOrExpression
|   ExclusiveOrExpression_no_ExclusiveOrExpression ExclusiveOrExpression(ExclusiveOrExpression)+
;
InclusiveOrExpression:
    InclusiveOrExpression_no_InclusiveOrExpression
|   InclusiveOrExpression_no_InclusiveOrExpression InclusiveOrExpression(InclusiveOrExpression)+
;
ConditionalAndExpression:
    ConditionalAndExpression_no_ConditionalAndExpression
|   ConditionalAndExpression_no_ConditionalAndExpression ConditionalAndExpression(ConditionalAndExpression)+
;
ConditionalOrExpression:
    ConditionalOrExpression_no_ConditionalOrExpression
|   ConditionalOrExpression_no_ConditionalOrExpression ConditionalOrExpression(ConditionalOrExpression)+
;
ConditionalExpression:
    ConditionalOrExpression
|   ConditionalOrExpression QUESTION Expression COLON (ConditionalExpression | LambdaExpression)
;
AssignmentExpression:
    ConditionalExpression
|   Assignment
;
Assignment = LeftHandSide AssignmentOperator Expression;
LeftHandSide:
    ExpressionName
|   FieldAccess
|   ArrayAccess
;
AssignmentOperator:
    EQ
|   STAREQ
|   DIVEQ
|   PERCENTEQ
|   PLUSEQ
|   MINUSEQ
|   LTLTEQ
|   GTGTEQ
|   GTGTGTEQ
|   ANDEQ
|   POWEQ
|   OREQ
;
LambdaExpression = LambdaParameters ARROW LambdaBody;
LambdaParameters:
    LPAREN RPAREN
|   LPAREN LambdaParameterList RPAREN
|   Identifier
;
LambdaParameterList:
    LambdaParameter
|   LambdaParameter (COMMA LambdaParameter)+
|   Identifier
|   Identifier (COMMA Identifier)+
;
LambdaParameter:
    LambdaParameterType VariableDeclaratorId
|   VariableModifier+ LambdaParameterType VariableDeclaratorId
|   VariableArityParameter
;
LambdaParameterType:
    UnannType
|   VAR_KW
;
LambdaBody:
    Expression
|   Block
;
SwitchExpression = SWITCH_KEYWORD LPAREN Expression RPAREN SwitchBlock;
ConstantExpression = Expression;
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
ModuleName(ModuleName):
    DOT Identifier
|   ModuleName(ModuleName) DOT Identifier
;
ModuleName_no_ModuleName = Identifier;
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
AmbiguousName(AmbiguousName):
    DOT Identifier
|   AmbiguousName(AmbiguousName) DOT Identifier
;
AmbiguousName_no_AmbiguousName = Identifier;
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
UnannClassType(UnannClassOrInterfaceType):
    DOT TypeIdentifier
|   UnannClassOrInterfaceType(UnannClassOrInterfaceType) DOT TypeIdentifier
|   DOT TypeIdentifier TypeArguments
|   UnannClassOrInterfaceType(UnannClassOrInterfaceType) DOT TypeIdentifier TypeArguments
|   DOT Annotation+ TypeIdentifier
|   UnannClassOrInterfaceType(UnannClassOrInterfaceType) DOT Annotation+ TypeIdentifier
|   DOT Annotation+ TypeIdentifier TypeArguments
|   UnannClassOrInterfaceType(UnannClassOrInterfaceType) DOT Annotation+ TypeIdentifier TypeArguments
;
UnannClassType_no_UnannClassOrInterfaceType:
    TypeIdentifier
|   TypeIdentifier TypeArguments
|   PackageName DOT TypeIdentifier
|   PackageName DOT TypeIdentifier TypeArguments
|   PackageName DOT Annotation+ TypeIdentifier
|   PackageName DOT Annotation+ TypeIdentifier TypeArguments
;
UnannInterfaceType(UnannClassOrInterfaceType) = UnannClassType(UnannClassOrInterfaceType);
UnannInterfaceType_no_UnannClassOrInterfaceType = UnannClassType_no_UnannClassOrInterfaceType;
UnannClassOrInterfaceType(UnannClassOrInterfaceType):
    UnannClassType(UnannClassOrInterfaceType)
|   UnannInterfaceType(UnannClassOrInterfaceType)
;
UnannClassOrInterfaceType_no_UnannClassOrInterfaceType:
    UnannClassType_no_UnannClassOrInterfaceType
|   UnannInterfaceType_no_UnannClassOrInterfaceType
;
ClassInstanceCreationExpression(Primary):
    DOT UnqualifiedClassInstanceCreationExpression
|   Primary(Primary) DOT UnqualifiedClassInstanceCreationExpression
;
ClassInstanceCreationExpression_no_Primary:
    UnqualifiedClassInstanceCreationExpression
|   ExpressionName DOT UnqualifiedClassInstanceCreationExpression
;
FieldAccess(Primary):
    DOT Identifier
|   Primary(Primary) DOT Identifier
;
FieldAccess_no_Primary = (SUPER_KEYWORD | TypeName DOT SUPER_KEYWORD) DOT Identifier;
ArrayAccess(Primary):
    ArrayAccess_Primary_no_ArrayAccess(Primary)
|   ArrayAccess_Primary_no_ArrayAccess(Primary) ArrayAccess(Primary, ArrayAccess(Primary))+
;
ArrayAccess_no_Primary:
    ExpressionName
|   ExpressionName Expression
;
MethodInvocation(Primary):
    DOT Identifier LPAREN RPAREN
|   Primary(Primary) DOT Identifier LPAREN RPAREN
|   DOT Identifier LPAREN ArgumentList RPAREN
|   Primary(Primary) DOT Identifier LPAREN ArgumentList RPAREN
|   DOT TypeArguments Identifier LPAREN RPAREN
|   Primary(Primary) DOT TypeArguments Identifier LPAREN RPAREN
|   DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   Primary(Primary) DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
;
MethodInvocation_no_Primary:
    MethodName LPAREN RPAREN
|   MethodName LPAREN ArgumentList RPAREN
|   TypeName DOT Identifier LPAREN RPAREN
|   TypeName DOT Identifier LPAREN ArgumentList RPAREN
|   TypeName DOT TypeArguments Identifier LPAREN RPAREN
|   TypeName DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   ExpressionName DOT Identifier LPAREN RPAREN
|   ExpressionName DOT Identifier LPAREN ArgumentList RPAREN
|   ExpressionName DOT TypeArguments Identifier LPAREN RPAREN
|   ExpressionName DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   SUPER_KEYWORD DOT Identifier RPAREN
|   SUPER_KEYWORD DOT Identifier ArgumentList RPAREN
|   SUPER_KEYWORD DOT Identifier LPAREN RPAREN
|   SUPER_KEYWORD DOT Identifier LPAREN ArgumentList RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier ArgumentList RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier LPAREN RPAREN
|   SUPER_KEYWORD DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
|   TypeName DOT SUPER_KEYWORD DOT Identifier LPAREN RPAREN
|   TypeName DOT SUPER_KEYWORD DOT Identifier LPAREN ArgumentList RPAREN
|   TypeName DOT SUPER_KEYWORD DOT TypeArguments Identifier LPAREN RPAREN
|   TypeName DOT SUPER_KEYWORD DOT TypeArguments Identifier LPAREN ArgumentList RPAREN
;
MethodReference(Primary):
    COLONCOLON Identifier
|   Primary(Primary) COLONCOLON Identifier
|   COLONCOLON TypeArguments Identifier
|   Primary(Primary) COLONCOLON TypeArguments Identifier
;
MethodReference_no_Primary:
    ExpressionName COLONCOLON Identifier
|   ExpressionName COLONCOLON TypeArguments Identifier
|   ReferenceType COLONCOLON Identifier
|   ReferenceType COLONCOLON TypeArguments Identifier
|   SUPER_KEYWORD COLONCOLON Identifier
|   SUPER_KEYWORD COLONCOLON TypeArguments Identifier
|   TypeName DOT SUPER_KEYWORD COLONCOLON Identifier
|   TypeName DOT SUPER_KEYWORD COLONCOLON TypeArguments Identifier
|   ClassType COLONCOLON NEW_KEYWORD
|   ClassType COLONCOLON TypeArguments NEW_KEYWORD
|   ArrayType COLONCOLON NEW_KEYWORD
;
PrimaryNoNewArray(Primary):
    ClassInstanceCreationExpression(Primary)
|   FieldAccess(Primary)
|   ArrayAccess(Primary)
|   MethodInvocation(Primary)
|   MethodReference(Primary)
;
PrimaryNoNewArray_no_Primary:
    Literal
|   ClassLiteral
|   THIS_KEYWORD
|   TypeName DOT THIS_KEYWORD
|   LPAREN Expression RPAREN
|   ClassInstanceCreationExpression_no_Primary
|   FieldAccess_no_Primary
|   ArrayAccess_no_Primary
|   MethodInvocation_no_Primary
|   MethodReference_no_Primary
;
Primary(Primary) = PrimaryNoNewArray(Primary);
Primary_no_Primary:
    PrimaryNoNewArray_no_Primary
|   ArrayCreationExpression
;
ArrayAccess(PrimaryNoNewArray):
    PrimaryNoNewArray(PrimaryNoNewArray)
|   Expression
|   PrimaryNoNewArray(PrimaryNoNewArray) Expression
;
ArrayAccess_no_PrimaryNoNewArray:
    ExpressionName
|   ExpressionName Expression
;
PrimaryNoNewArray(PrimaryNoNewArray) = ArrayAccess(PrimaryNoNewArray);
PrimaryNoNewArray_no_PrimaryNoNewArray:
    Literal
|   ClassLiteral
|   THIS_KEYWORD
|   TypeName DOT THIS_KEYWORD
|   LPAREN Expression RPAREN
|   ClassInstanceCreationExpression
|   FieldAccess
|   ArrayAccess_no_PrimaryNoNewArray
|   MethodInvocation
|   MethodReference
;
PostIncrementExpression(PostfixExpression):
    PLUSPLUS
|   PostfixExpression(PostfixExpression) PLUSPLUS
;
PostDecrementExpression(PostfixExpression):
    MINUSMINUS
|   PostfixExpression(PostfixExpression) MINUSMINUS
;
PostfixExpression(PostfixExpression):
    PostIncrementExpression(PostfixExpression)
|   PostDecrementExpression(PostfixExpression)
;
PostfixExpression_no_PostfixExpression:
    Primary
|   ExpressionName
;
MultiplicativeExpression(MultiplicativeExpression):
    (STAR | DIV | PERCENT) UnaryExpression
|   MultiplicativeExpression(MultiplicativeExpression) (STAR | DIV | PERCENT) UnaryExpression
;
MultiplicativeExpression_no_MultiplicativeExpression = UnaryExpression;
AdditiveExpression(AdditiveExpression):
    (PLUS | MINUS) MultiplicativeExpression
|   AdditiveExpression(AdditiveExpression) (PLUS | MINUS) MultiplicativeExpression
;
AdditiveExpression_no_AdditiveExpression = MultiplicativeExpression;
ShiftExpression(ShiftExpression):
    (LTLT | GTGT | GTGTGT) AdditiveExpression
|   ShiftExpression(ShiftExpression) (LTLT | GTGT | GTGTGT) AdditiveExpression
;
ShiftExpression_no_ShiftExpression = AdditiveExpression;
RelationalExpression(RelationalExpression):
    (LT | GT | LTEQ | GTEQ) ShiftExpression
|   RelationalExpression(RelationalExpression) (LT | GT | LTEQ | GTEQ) ShiftExpression
|   INSTANCEOF_KEYWORD ReferenceType
|   RelationalExpression(RelationalExpression) INSTANCEOF_KEYWORD ReferenceType
;
RelationalExpression_no_RelationalExpression = ShiftExpression;
EqualityExpression(EqualityExpression):
    (EQEQ | NEQ) RelationalExpression
|   EqualityExpression(EqualityExpression) (EQEQ | NEQ) RelationalExpression
;
EqualityExpression_no_EqualityExpression = RelationalExpression;
AndExpression(AndExpression):
    AND EqualityExpression
|   AndExpression(AndExpression) AND EqualityExpression
;
AndExpression_no_AndExpression = EqualityExpression;
ExclusiveOrExpression(ExclusiveOrExpression):
    XOR AndExpression
|   ExclusiveOrExpression(ExclusiveOrExpression) XOR AndExpression
;
ExclusiveOrExpression_no_ExclusiveOrExpression = AndExpression;
InclusiveOrExpression(InclusiveOrExpression):
    OR ExclusiveOrExpression
|   InclusiveOrExpression(InclusiveOrExpression) OR ExclusiveOrExpression
;
InclusiveOrExpression_no_InclusiveOrExpression = ExclusiveOrExpression;
ConditionalAndExpression(ConditionalAndExpression):
    ANDAND InclusiveOrExpression
|   ConditionalAndExpression(ConditionalAndExpression) ANDAND InclusiveOrExpression
;
ConditionalAndExpression_no_ConditionalAndExpression = InclusiveOrExpression;
ConditionalOrExpression(ConditionalOrExpression):
    OROR ConditionalAndExpression
|   ConditionalOrExpression(ConditionalOrExpression) OROR ConditionalAndExpression
;
ConditionalOrExpression_no_ConditionalOrExpression = ConditionalAndExpression;
PrimaryNoNewArray(Primary, ArrayAccess(Primary)) = ArrayAccess(ArrayAccess)(Primary);
PrimaryNoNewArray_Primary_no_ArrayAccess(Primary):
    ClassInstanceCreationExpression(Primary)
|   FieldAccess(Primary)
|   MethodInvocation(Primary)
|   MethodReference(Primary)
;
ArrayAccess(Primary, ArrayAccess(Primary)):
    PrimaryNoNewArray(Primary, ArrayAccess(Primary))
|   Expression
|   PrimaryNoNewArray(Primary, ArrayAccess(Primary)) Expression
;
ArrayAccess_Primary_no_ArrayAccess(Primary):
    PrimaryNoNewArray_Primary_no_ArrayAccess(Primary)
|   PrimaryNoNewArray_Primary_no_ArrayAccess(Primary) Expression
;

