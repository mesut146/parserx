include "lexer-jls.g"

%start = CompilationUnit;

TypeIdentifier: Identifier;
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
PrimitiveType: Annotation* PrimitiveTypeg1;
PrimitiveTypeg1:
    NumericType
|   BOOLEAN_KEYWORD
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
ClassOrInterfaceType: ClassOrInterfaceType_no_ClassOrInterfaceType ClassOrInterfaceType1(ClassOrInterfaceType)*;
ClassOrInterfaceType_no_ClassOrInterfaceType:
    ClassType_no_ClassOrInterfaceType
|   InterfaceType_no_ClassOrInterfaceType
;
ClassOrInterfaceType1(ClassOrInterfaceType):
    ClassType1(ClassOrInterfaceType)
|   InterfaceType1(ClassOrInterfaceType)
;
ClassType:
    Annotation* TypeIdentifier TypeArguments?
|   PackageName DOT Annotation* TypeIdentifier TypeArguments?
|   ClassOrInterfaceType DOT Annotation* TypeIdentifier TypeArguments?
;
ClassType_no_ClassOrInterfaceType:
    Annotation* TypeIdentifier TypeArguments?
|   PackageName DOT Annotation* TypeIdentifier TypeArguments?
;
ClassType1(ClassOrInterfaceType): ClassOrInterfaceType(ClassOrInterfaceType) DOT Annotation* TypeIdentifier TypeArguments?;
InterfaceType: ClassType;
InterfaceType_no_ClassOrInterfaceType: ClassType_no_ClassOrInterfaceType;
InterfaceType1(ClassOrInterfaceType): ClassType_no_ClassOrInterfaceType;
TypeVariable: Annotation* TypeIdentifier;
ArrayType:
    PrimitiveType Dims
|   ClassOrInterfaceType Dims
|   TypeVariable Dims
;
Dims: Annotation* LBRACKET RBRACKET Dimsg1*;
Dimsg1: Annotation* LBRACKET RBRACKET;
TypeParameter: TypeParameterModifier* TypeIdentifier TypeBound?;
TypeParameterModifier: Annotation;
TypeBound: EXTENDS_KEYWORD TypeBoundg1;
TypeBoundg1:
    TypeVariable
|   ClassOrInterfaceType AdditionalBound*
;
AdditionalBound: AND InterfaceType;
TypeArguments: LT TypeArgumentList GT;
TypeArgumentList: TypeArgument TypeArgumentListg1*;
TypeArgumentListg1: COMMA TypeArgument;
TypeArgument:
    ReferenceType
|   Wildcard
;
Wildcard: Annotation* QUESTION WildcardBounds?;
WildcardBounds:
    EXTENDS_KEYWORD ReferenceType
|   SUPER_KEYWORD ReferenceType
;
CompilationUnit:
    OrdinaryCompilationUnit
|   ModularCompilationUnit
;
OrdinaryCompilationUnit: PackageDeclaration? ImportDeclaration* TopLevelClassOrInterfaceDeclaration*;
ModularCompilationUnit: ImportDeclaration* ModuleDeclaration;

PackageDeclaration: PackageModifier* PACKAGE_KEYWORD Identifier PackageDeclarationg1*;
PackageDeclarationg1: DOT Identifier;
PackageModifier: Annotation;

ImportDeclaration:
    SingleTypeImportDeclaration
|   TypeImportOnDemandDeclaration
|   SingleStaticImportDeclaration
|   StaticImportOnDemandDeclaration
;
SingleTypeImportDeclaration: IMPORT_KEYWORD TypeName;
TypeImportOnDemandDeclaration: IMPORT_KEYWORD PackageOrTypeName DOT STAR;
SingleStaticImportDeclaration: IMPORT_KEYWORD STATIC_KEYWORD TypeName DOT Identifier;
StaticImportOnDemandDeclaration: IMPORT_KEYWORD STATIC_KEYWORD TypeName DOT STAR;
TopLevelClassOrInterfaceDeclaration:
    ClassDeclaration
|   InterfaceDeclaration
;
ModuleName: ModuleName_no_ModuleName ModuleName1(ModuleName)*;
ModuleName_no_ModuleName: Identifier;
ModuleName1(ModuleName): ModuleName(ModuleName) DOT Identifier;
PackageName: PackageName_no_PackageName PackageName1(PackageName)*;
PackageName_no_PackageName: Identifier;
PackageName1(PackageName): PackageName(PackageName) DOT Identifier;
TypeName:
    TypeIdentifier
|   PackageOrTypeName DOT TypeIdentifier
;
PackageOrTypeName: PackageOrTypeName_no_PackageOrTypeName PackageOrTypeName1(PackageOrTypeName)*;
PackageOrTypeName_no_PackageOrTypeName: Identifier;
PackageOrTypeName1(PackageOrTypeName): PackageOrTypeName(PackageOrTypeName) DOT Identifier;
ExpressionName:
    Identifier
|   AmbiguousName DOT Identifier
;
MethodName: UnqualifiedMethodIdentifier;
AmbiguousName: AmbiguousName_no_AmbiguousName AmbiguousName1(AmbiguousName)*;
AmbiguousName_no_AmbiguousName: Identifier;
AmbiguousName1(AmbiguousName): AmbiguousName(AmbiguousName) DOT Identifier;
ModuleDeclaration: Annotation* OPEN_KEYWORD? MODULE_KW Identifier ModuleDeclarationg1*;
ModuleDeclarationg1: DOT Identifier;
Annotation:
    NormalAnnotation
|   MarkerAnnotation
|   SingleElementAnnotation
;
NormalAnnotation: AT TypeName LPAREN ElementValuePairList? RPAREN;
ElementValuePairList: ElementValuePair ElementValuePairListg1*;
ElementValuePairListg1: COMMA ElementValuePair;
ElementValuePair: Identifier EQ ElementValue;
ElementValue:
    ConditionalExpression
|   ElementValueArrayInitializer
|   Annotation
;
ElementValueArrayInitializer: LBRACE ElementValueList? COMMA? RBRACE;
ElementValueList: ElementValue ElementValueListg1*;
ElementValueListg1: COMMA ElementValue;
MarkerAnnotation: AT TypeName;
SingleElementAnnotation: AT TypeName LPAREN ElementValue RPAREN;
ModuleDirective:
    REQUIRES_KW RequiresModifier* ModuleName
|   EXPORTS_KW PackageName ModuleDirectiveg1?
|   OPENS_KW PackageName ModuleDirectiveg3?
|   USES TypeName
|   PROVIDES_KW TypeName WITH_KW TypeName ModuleDirectiveg5*
;
ModuleDirectiveg5: COMMA TypeName;
ModuleDirectiveg4: COMMA ModuleName;
ModuleDirectiveg3: TO_KW ModuleName ModuleDirectiveg4*;
ModuleDirectiveg2: COMMA ModuleName;
ModuleDirectiveg1: TO_KW ModuleName ModuleDirectiveg2*;
RequiresModifier:
    TRANSITIVE_KW
|   STATIC_KEYWORD
;
ClassDeclaration:
    NormalClassDeclaration
|   EnumDeclaration
|   RecordDeclaration
;
NormalClassDeclaration: ClassModifier* CLASS_KEYWORD TypeIdentifier TypeParameters? ClassExtends? ClassImplements? ClassBody;
ClassModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PROTECTED_KEYWORD
|   PRIVATE_KEYWORD ABSTRACT_KEYWORD
|   STATIC_KEYWORD
|   FINAL_KEYWORD
|   STRICTFP_KEYWORD
;
ConstructorDeclaration: ConstructorModifier* ConstructorDeclarator Throws? ConstructorBody;
ConstructorDeclarator: TypeParameters? SimpleTypeName LPAREN ConstructorDeclaratorg1? FormalParameterList? RPAREN;
ConstructorDeclaratorg1: ReceiverParameter COMMA;
SimpleTypeName: TypeIdentifier;
ConstructorModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PROTECTED_KEYWORD
|   PRIVATE_KEYWORD
;
ConstructorBody: LBRACE ExplicitConstructorInvocation? BlockStatements? RBRACE;
ExplicitConstructorInvocation:
    TypeArguments? THIS_KEYWORD LPAREN ArgumentList? RPAREN SEMI
|   TypeArguments? SUPER_KEYWORD LPAREN ArgumentList? RPAREN SEMI
|   ExpressionName DOT TypeArguments? SUPER_KEYWORD LPAREN ArgumentList? RPAREN SEMI
|   Primary DOT TypeArguments? SUPER_KEYWORD LPAREN ArgumentList? RPAREN SEMI
;
EnumDeclaration: ClassModifier* ENUM_KEYWORD TypeIdentifier ClassImplements? EnumBody;
EnumBody: LBRACE EnumConstantList? COMMA? EnumBodyDeclarations? RBRACE;
EnumConstantList: EnumConstant EnumConstantListg1*;
EnumConstantListg1: COMMA EnumConstant;
EnumConstant: EnumConstantModifier* Identifier EnumConstantg1? ClassBody?;
EnumConstantg1: LPAREN ArgumentList? RPAREN;
EnumConstantModifier: Annotation;
EnumBodyDeclarations: SEMI ClassBodyDeclaration*;
RecordDeclaration: ClassModifier* RECORD_KW TypeIdentifier TypeParameters? RecordHeader ClassImplements? RecordBody;
RecordHeader: LPAREN RecordComponentList? RPAREN;
RecordComponentList: RecordComponent RecordComponentListg1*;
RecordComponentListg1: COMMA RecordComponent;
RecordComponent:
    RecordComponentModifier* UnannType Identifier
|   VariableArityRecordComponent
;
VariableArityRecordComponent: RecordComponentModifier* UnannType Annotation* VARARGS Identifier;
RecordComponentModifier: Annotation;
RecordBody: LBRACE RecordBodyDeclaration* RBRACE;
RecordBodyDeclaration:
    ClassBodyDeclaration
|   CompactConstructorDeclaration
;
CompactConstructorDeclaration: ConstructorModifier* SimpleTypeName ConstructorBody;
TypeParameters: LT TypeParameterList GT;
TypeParameterList: TypeParameter TypeParameterListg1*;
TypeParameterListg1: COMMA TypeParameter;
ClassExtends: EXTENDS_KEYWORD ClassType;
ClassImplements: IMPLEMENTS_KEYWORD InterfaceTypeList;
InterfaceTypeList: InterfaceType InterfaceTypeListg1*;
InterfaceTypeListg1: COMMA InterfaceType;
ClassBody: LBRACE ClassBodyDeclaration* RBRACE;
ClassBodyDeclaration:
    ClassMemberDeclaration
|   InstanceInitializer
|   StaticInitializer
|   ConstructorDeclaration
;
InstanceInitializer: Block;
StaticInitializer: STATIC_KEYWORD Block;
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
NormalInterfaceDeclaration: InterfaceModifier* INTERFACE_KEYWORD TypeIdentifier TypeParameters? InterfaceExtends? InterfaceBody;
InterfaceModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PROTECTED_KEYWORD
|   PRIVATE_KEYWORD
|   ABSTRACT_KEYWORD
|   STATIC_KEYWORD
|   STRICTFP_KEYWORD
;
InterfaceExtends: EXTENDS_KEYWORD InterfaceTypeList;
InterfaceBody: LBRACE InterfaceMemberDeclaration* RBRACE;
InterfaceMemberDeclaration:
    ConstantDeclaration
|   InterfaceMethodDeclaration
|   ClassDeclaration
|   InterfaceDeclaration
|   SEMI
;
ConstantDeclaration: ConstantModifier* UnannType VariableDeclaratorList SEMI;
ConstantModifier:
    Annotation
|   PUBLIC_KEYWORD
|   STATIC_KEYWORD
|   FINAL_KEYWORD
;
InterfaceMethodDeclaration: InterfaceMethodModifier* MethodHeader MethodBody;
InterfaceMethodModifier:
    Annotation
|   PUBLIC_KEYWORD
|   PRIVATE_KEYWORD
|   ABSTRACT_KEYWORD
|   DEFAULT_KEYWORD
|   STATIC_KEYWORD
|   STRICTFP_KEYWORD
;
AnnotationInterfaceDeclaration: InterfaceModifier* AT INTERFACE_KEYWORD TypeIdentifier AnnotationInterfaceBody;
AnnotationInterfaceBody: LBRACE AnnotationInterfaceMemberDeclaration* RBRACE;
AnnotationInterfaceMemberDeclaration:
    AnnotationInterfaceElementDeclaration
|   ConstantDeclaration
|   ClassDeclaration
|   InterfaceDeclaration
|   SEMI
;
AnnotationInterfaceElementDeclaration: AnnotationInterfaceElementModifier* UnannType Identifier LPAREN RPAREN Dims? DefaultValue? SEMI;
AnnotationInterfaceElementModifier:
    Annotation
|   PUBLIC_KEYWORD
|   ABSTRACT_KEYWORD
;
DefaultValue: DEFAULT_KEYWORD ElementValue;
ArrayInitializer: LBRACE VariableInitializerList? COMMA? RBRACE;
VariableInitializerList: VariableInitializer VariableInitializerListg1*;
VariableInitializerListg1: COMMA VariableInitializer;
FieldDeclaration: FieldModifier* UnannType VariableDeclaratorList;
VariableDeclaratorList: VariableDeclarator VariableDeclaratorListg1*;
VariableDeclaratorListg1: COMMA VariableDeclarator;
VariableDeclarator: VariableDeclaratorId VariableDeclaratorg1?;
VariableDeclaratorg1: EQ VariableInitializer;
VariableDeclaratorId: Identifier Dims?;
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
UnannClassOrInterfaceType: UnannClassOrInterfaceType_no_UnannClassOrInterfaceType UnannClassOrInterfaceType1(UnannClassOrInterfaceType)*;
UnannClassOrInterfaceType_no_UnannClassOrInterfaceType:
    UnannClassType_no_UnannClassOrInterfaceType
|   UnannInterfaceType_no_UnannClassOrInterfaceType
;
UnannClassOrInterfaceType1(UnannClassOrInterfaceType):
    UnannClassType1(UnannClassOrInterfaceType)
|   UnannInterfaceType1(UnannClassOrInterfaceType)
;
UnannClassType:
    TypeIdentifier TypeArguments?
|   PackageName DOT Annotation* TypeIdentifier TypeArguments?
|   UnannClassOrInterfaceType DOT Annotation* TypeIdentifier TypeArguments?
;
UnannClassType_no_UnannClassOrInterfaceType:
    TypeIdentifier TypeArguments?
|   PackageName DOT Annotation* TypeIdentifier TypeArguments?
;
UnannClassType1(UnannClassOrInterfaceType): UnannClassOrInterfaceType(UnannClassOrInterfaceType) DOT Annotation* TypeIdentifier TypeArguments?;
UnannInterfaceType: UnannClassType;
UnannInterfaceType_no_UnannClassOrInterfaceType: UnannClassType_no_UnannClassOrInterfaceType;
UnannInterfaceType1(UnannClassOrInterfaceType): UnannClassType_no_UnannClassOrInterfaceType;
UnannTypeVariable: TypeIdentifier;
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
MethodDeclaration: MethodModifier* MethodHeader MethodBody;
MethodHeader:
    Result MethodDeclarator Throws?
|   TypeParameters Annotation* Result MethodDeclarator Throws?
;
MethodDeclarator: Identifier LPAREN MethodDeclaratorg1? FormalParameterList? RPAREN Dims?;
MethodDeclaratorg1: ReceiverParameter COMMA;
ReceiverParameter: Annotation* UnannType ReceiverParameterg1? THIS_KEYWORD;
ReceiverParameterg1: Identifier DOT;
FormalParameterList: FormalParameter FormalParameterListg1*;
FormalParameterListg1: COMMA FormalParameter;
FormalParameter:
    VariableModifier* UnannType VariableDeclaratorId
|   VariableArityParameter
;
VariableArityParameter: VariableModifier* UnannType Annotation* VARARGS Identifier;
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
Throws: THROWS_KEYWORD ExceptionTypeList;
ExceptionTypeList: ExceptionType ExceptionTypeListg1*;
ExceptionTypeListg1: COMMA ExceptionType;
ExceptionType:
    ClassType
|   TypeVariable
;
MethodBody: Block;
Block: LBRACE BlockStatements? RBRACE;
BlockStatements: BlockStatement BlockStatement*;
BlockStatement:
    LocalClassOrInterfaceDeclaration
|   LocalVariableDeclarationStatement
|   Statement
;
LocalClassOrInterfaceDeclaration:
    ClassDeclaration
|   NormalInterfaceDeclaration
;
LocalVariableDeclaration: VariableModifier* LocalVariableType VariableDeclaratorList;
LocalVariableType:
    UnannType
|   VAR_KW
;
LocalVariableDeclarationStatement: LocalVariableDeclaration SEMI;
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
EmptyStatement: SEMI;
LabeledStatement: Identifier COLON Statement;
LabeledStatementNoShortIf: Identifier COLON StatementNoShortIf;
ExpressionStatement: StatementExpression SEMI;
StatementExpression:
    Assignment
|   PreIncrementExpression
|   PreDecrementExpression
|   PostIncrementExpression
|   PostDecrementExpression
|   MethodInvocation
|   ClassInstanceCreationExpression
;
IfThenStatement: IF_KEYWORD LPAREN Expression RPAREN Statement;
IfThenElseStatement: IF_KEYWORD LPAREN Expression RPAREN StatementNoShortIf ELSE_KEYWORD Statement;
IfThenElseStatementNoShortIf: IF_KEYWORD LPAREN Expression RPAREN StatementNoShortIf ELSE_KEYWORD StatementNoShortIf;
AssertStatement: ASSERT_KEYWORD Expression AssertStatementg1? SEMI;
AssertStatementg1: COLON Expression;
SwitchStatement: SWITCH_KEYWORD LPAREN Expression RPAREN SwitchBlock;
SwitchBlock:
    LBRACE SwitchRule SwitchRule* RBRACE
|   LBRACE SwitchBlockStatementGroup* SwitchBlockg1* RBRACE
;
SwitchBlockg1: SwitchLabel COLON;
SwitchRule: SwitchLabel ARROW SwitchRuleg1;
SwitchRuleg1:
    Expression SEMI
|   Block
|   ThrowStatement
;
SwitchBlockStatementGroup: SwitchLabel COLON SwitchBlockStatementGroupg1* BlockStatements;
SwitchBlockStatementGroupg1: SwitchLabel COLON;
SwitchLabel:
    CASE_KEYWORD CaseConstant SwitchLabelg1*
|   DEFAULT_KEYWORD
;
SwitchLabelg1: COMMA CaseConstant;
CaseConstant: ConditionalExpression;
WhileStatement: WHILE_KEYWORD LPAREN Expression RPAREN Statement;
WhileStatementNoShortIf: WHILE_KEYWORD LPAREN Expression RPAREN StatementNoShortIf;
DoStatement: DO_KEYWORD Statement WHILE_KEYWORD LPAREN Expression RPAREN SEMI;
ForStatement:
    BasicForStatement
|   EnhancedForStatement
;
ForStatementNoShortIf:
    BasicForStatementNoShortIf
|   EnhancedForStatementNoShortIf
;
BasicForStatement: FOR_KEYWORD LPAREN ForInit? SEMI Expression? SEMI ForUpdate? RPAREN Statement;
BasicForStatementNoShortIf: FOR_KEYWORD LPAREN ForInit? SEMI Expression? SEMI ForUpdate? RPAREN StatementNoShortIf;
ForInit:
    StatementExpressionList
|   LocalVariableDeclaration
;
ForUpdate: StatementExpressionList;
StatementExpressionList: StatementExpression StatementExpressionListg1*;
StatementExpressionListg1: COMMA StatementExpression;
EnhancedForStatement: FOR_KEYWORD LPAREN LocalVariableDeclaration COLON Expression RPAREN Statement;
EnhancedForStatementNoShortIf: FOR_KEYWORD LPAREN LocalVariableDeclaration COLON Expression RPAREN StatementNoShortIf;
BreakStatement: BREAK_KEYWORD Identifier? SEMI;
ContinueStatement: CONTINUE_KEYWORD Identifier? SEMI;
ReturnStatement: RETURN_KEYWORD Expression? SEMI;
ThrowStatement: THROW_KEYWORD Expression SEMI;
SynchronizedStatement: SYNCHRONIZED_KEYWORD LPAREN Expression RPAREN Block;
TryStatement:
    TRY_KEYWORD Block TryStatementg1
|   TryWithResourcesStatement
;
TryStatementg1:
    Catches Finally?
|   Finally
;
Catches: CatchClause CatchClause*;
CatchClause: CATCH_KEYWORD LPAREN CatchFormalParameter RPAREN Block;
CatchFormalParameter: VariableModifier* CatchType VariableDeclaratorId;
CatchType: UnannClassType CatchTypeg1*;
CatchTypeg1: OR ClassType;
Finally: FINALLY_KEYWORD Block;
TryWithResourcesStatement: TRY_KEYWORD ResourceSpecification Block Catches? Finally?;
ResourceSpecification: LPAREN ResourceList SEMI? RPAREN;
ResourceList: Resource ResourceListg1*;
ResourceListg1: SEMI Resource;
Resource:
    LocalVariableDeclaration
|   VariableAccess
;
VariableAccess:
    ExpressionName
|   FieldAccess
;
YieldStatement: YIELD_KW Expression SEMI;
Expression:
    LambdaExpression
|   AssignmentExpression
;
Primary: Primary_no_Primary Primary1(Primary)*;
Primary_no_Primary:
    PrimaryNoNewArray_no_Primary
|   ArrayCreationExpression
;
Primary1(Primary): PrimaryNoNewArray1(Primary);
PrimaryNoNewArray: PrimaryNoNewArray_no_PrimaryNoNewArray PrimaryNoNewArray2(PrimaryNoNewArray)*;
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
PrimaryNoNewArray2(PrimaryNoNewArray): ArrayAccess2(PrimaryNoNewArray);
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
PrimaryNoNewArray1(Primary): PrimaryNoNewArray_no_PrimaryNoNewArray1(Primary) PrimaryNoNewArray3(Primary, PrimaryNoNewArray1(Primary))*;
PrimaryNoNewArray_no_PrimaryNoNewArray1(Primary):
    ClassInstanceCreationExpression1(Primary)
|   FieldAccess1(Primary)
|   MethodInvocation1(Primary)
|   MethodReference1(Primary)
;
PrimaryNoNewArray3(Primary, PrimaryNoNewArray1(Primary)): ArrayAccess3(Primary, PrimaryNoNewArray1(Primary));
ClassLiteral:
    TypeName ClassLiteralg1* DOT CLASS_KEYWORD
|   NumericType ClassLiteralg2* DOT CLASS_KEYWORD
|   BOOLEAN_KEYWORD ClassLiteralg3* DOT CLASS_KEYWORD
|   VOID_KEYWORD DOT CLASS_KEYWORD
;
ClassLiteralg3: LBRACKET RBRACKET;
ClassLiteralg2: LBRACKET RBRACKET;
ClassLiteralg1: LBRACKET RBRACKET;
ClassInstanceCreationExpression:
    UnqualifiedClassInstanceCreationExpression
|   ExpressionName DOT UnqualifiedClassInstanceCreationExpression
|   Primary DOT UnqualifiedClassInstanceCreationExpression
;
ClassInstanceCreationExpression_no_Primary:
    UnqualifiedClassInstanceCreationExpression
|   ExpressionName DOT UnqualifiedClassInstanceCreationExpression
;
ClassInstanceCreationExpression1(Primary): Primary(Primary) DOT UnqualifiedClassInstanceCreationExpression;
UnqualifiedClassInstanceCreationExpression: NEW_KEYWORD TypeArguments? ClassOrInterfaceTypeToInstantiate LPAREN ArgumentList? RPAREN ClassBody?;
ClassOrInterfaceTypeToInstantiate: Annotation* Identifier ClassOrInterfaceTypeToInstantiateg1* TypeArgumentsOrDiamond?;
ClassOrInterfaceTypeToInstantiateg1: DOT Annotation* Identifier;
TypeArgumentsOrDiamond:
    TypeArguments
|   LT GT
;
ArrayCreationExpression: NEW_KEYWORD ArrayCreationExpressiong1 ArrayCreationExpressiong2;
ArrayCreationExpressiong2:
    DimExprs Dims?
|   Dims ArrayInitializer
;
ArrayCreationExpressiong1:
    PrimitiveType
|   ClassOrInterfaceType
;
DimExprs: DimExpr DimExpr*;
DimExpr: Annotation* Expression?;
ArrayAccess:
    ExpressionName Expression?
|   PrimaryNoNewArray Expression?
;
ArrayAccess_no_PrimaryNoNewArray: ExpressionName Expression?;
ArrayAccess2(PrimaryNoNewArray): PrimaryNoNewArray(PrimaryNoNewArray) Expression?;
ArrayAccess_no_Primary: ExpressionName Expression?;
ArrayAccess1(Primary): PrimaryNoNewArray1(Primary) Expression?;
ArrayAccess3(Primary, PrimaryNoNewArray1(Primary)): PrimaryNoNewArray1(Primary)(PrimaryNoNewArray1) Expression?;
FieldAccess: FieldAccessg1 DOT Identifier;
FieldAccess_no_Primary: FieldAccessg1_no_Primary DOT Identifier;
FieldAccess1(Primary): FieldAccessg11(Primary) DOT Identifier;
FieldAccessg1:
    Primary
|   SUPER_KEYWORD
|   TypeName DOT SUPER_KEYWORD
;
FieldAccessg1_no_Primary:
    SUPER_KEYWORD
|   TypeName DOT SUPER_KEYWORD
;
FieldAccessg11(Primary): Primary(Primary);
MethodInvocation:
    MethodName LPAREN ArgumentList? RPAREN
|   TypeName DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN
|   ExpressionName DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN
|   Primary DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN
|   SUPER_KEYWORD DOT TypeArguments? Identifier LPAREN? ArgumentList? RPAREN
|   TypeName DOT SUPER_KEYWORD DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN
;
MethodInvocation_no_Primary:
    MethodName LPAREN ArgumentList? RPAREN
|   TypeName DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN
|   ExpressionName DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN
|   SUPER_KEYWORD DOT TypeArguments? Identifier LPAREN? ArgumentList? RPAREN
|   TypeName DOT SUPER_KEYWORD DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN
;
MethodInvocation1(Primary): Primary(Primary) DOT TypeArguments? Identifier LPAREN ArgumentList? RPAREN;
ArgumentList: Expression ArgumentListg1*;
ArgumentListg1: COMMA Expression;
MethodReference:
    ExpressionName COLONCOLON TypeArguments? Identifier
|   Primary COLONCOLON TypeArguments? Identifier
|   ReferenceType COLONCOLON TypeArguments? Identifier
|   SUPER_KEYWORD COLONCOLON TypeArguments? Identifier
|   TypeName DOT SUPER_KEYWORD COLONCOLON TypeArguments? Identifier
|   ClassType COLONCOLON TypeArguments? NEW_KEYWORD
|   ArrayType COLONCOLON NEW_KEYWORD
;
MethodReference_no_Primary:
    ExpressionName COLONCOLON TypeArguments? Identifier
|   ReferenceType COLONCOLON TypeArguments? Identifier
|   SUPER_KEYWORD COLONCOLON TypeArguments? Identifier
|   TypeName DOT SUPER_KEYWORD COLONCOLON TypeArguments? Identifier
|   ClassType COLONCOLON TypeArguments? NEW_KEYWORD
|   ArrayType COLONCOLON NEW_KEYWORD
;
MethodReference1(Primary): Primary(Primary) COLONCOLON TypeArguments? Identifier;
PostfixExpression: PostfixExpression_no_PostfixExpression PostfixExpression1(PostfixExpression)*;
PostfixExpression_no_PostfixExpression:
    Primary
|   ExpressionName
;
PostfixExpression1(PostfixExpression):
    PostIncrementExpression1(PostfixExpression)
|   PostDecrementExpression1(PostfixExpression)
;
PostIncrementExpression: PostfixExpression PLUSPLUS;
PostIncrementExpression1(PostfixExpression): PostfixExpression(PostfixExpression) PLUSPLUS;
PostDecrementExpression: PostfixExpression MINUSMINUS;
PostDecrementExpression1(PostfixExpression): PostfixExpression(PostfixExpression) MINUSMINUS;
UnaryExpression:
    PreIncrementExpression
|   PreDecrementExpression
|   PLUS UnaryExpression
|   MINUS UnaryExpression
|   UnaryExpressionNotPlusMinus
;
PreIncrementExpression: PLUSPLUS UnaryExpression;
PreDecrementExpression: MINUSMINUS UnaryExpression;
UnaryExpressionNotPlusMinus:
    PostfixExpression
|   TILDE UnaryExpression
|   BANG UnaryExpression
|   CastExpression
|   SwitchExpression
;
CastExpression:
    LPAREN PrimitiveType RPAREN UnaryExpression
|   LPAREN ReferenceType AdditionalBound* RPAREN UnaryExpressionNotPlusMinus
|   LPAREN ReferenceType AdditionalBound* RPAREN LambdaExpression
;
MultiplicativeExpression: MultiplicativeExpression_no_MultiplicativeExpression MultiplicativeExpression1(MultiplicativeExpression)*;
MultiplicativeExpression_no_MultiplicativeExpression: UnaryExpression;
MultiplicativeExpression1(MultiplicativeExpression): MultiplicativeExpression(MultiplicativeExpression) MultiplicativeExpressiong1 UnaryExpression;
MultiplicativeExpressiong1:
    STAR
|   DIV
|   PERCENT
;
AdditiveExpression: AdditiveExpression_no_AdditiveExpression AdditiveExpression1(AdditiveExpression)*;
AdditiveExpression_no_AdditiveExpression: MultiplicativeExpression;
AdditiveExpression1(AdditiveExpression): AdditiveExpression(AdditiveExpression) AdditiveExpressiong1 MultiplicativeExpression;
AdditiveExpressiong1:
    PLUS
|   MINUS
;
ShiftExpression: ShiftExpression_no_ShiftExpression ShiftExpression1(ShiftExpression)*;
ShiftExpression_no_ShiftExpression: AdditiveExpression;
ShiftExpression1(ShiftExpression): ShiftExpression(ShiftExpression) ShiftExpressiong1 AdditiveExpression;
ShiftExpressiong1:
    LTLT
|   GT GT
|   GT GT GT
;
RelationalExpression: RelationalExpression_no_RelationalExpression RelationalExpression1(RelationalExpression)*;
RelationalExpression_no_RelationalExpression: ShiftExpression;
RelationalExpression1(RelationalExpression):
    RelationalExpression(RelationalExpression) RelationalExpressiong1 ShiftExpression
|   RelationalExpression(RelationalExpression) INSTANCEOF_KEYWORD ReferenceType
;
RelationalExpressiong1:
    LT
|   GT
|   LTEQ
|   GTEQ
;
EqualityExpression: EqualityExpression_no_EqualityExpression EqualityExpression1(EqualityExpression)*;
EqualityExpression_no_EqualityExpression: RelationalExpression;
EqualityExpression1(EqualityExpression): EqualityExpression(EqualityExpression) EqualityExpressiong1 RelationalExpression;
EqualityExpressiong1:
    EQEQ
|   NEQ
;
AndExpression: AndExpression_no_AndExpression AndExpression1(AndExpression)*;
AndExpression_no_AndExpression: EqualityExpression;
AndExpression1(AndExpression): AndExpression(AndExpression) AND EqualityExpression;
ExclusiveOrExpression: ExclusiveOrExpression_no_ExclusiveOrExpression ExclusiveOrExpression1(ExclusiveOrExpression)*;
ExclusiveOrExpression_no_ExclusiveOrExpression: AndExpression;
ExclusiveOrExpression1(ExclusiveOrExpression): ExclusiveOrExpression(ExclusiveOrExpression) XOR AndExpression;
InclusiveOrExpression: InclusiveOrExpression_no_InclusiveOrExpression InclusiveOrExpression1(InclusiveOrExpression)*;
InclusiveOrExpression_no_InclusiveOrExpression: ExclusiveOrExpression;
InclusiveOrExpression1(InclusiveOrExpression): InclusiveOrExpression(InclusiveOrExpression) OR ExclusiveOrExpression;
ConditionalAndExpression: ConditionalAndExpression_no_ConditionalAndExpression ConditionalAndExpression1(ConditionalAndExpression)*;
ConditionalAndExpression_no_ConditionalAndExpression: InclusiveOrExpression;
ConditionalAndExpression1(ConditionalAndExpression): ConditionalAndExpression(ConditionalAndExpression) ANDAND InclusiveOrExpression;
ConditionalOrExpression: ConditionalOrExpression_no_ConditionalOrExpression ConditionalOrExpression1(ConditionalOrExpression)*;
ConditionalOrExpression_no_ConditionalOrExpression: ConditionalAndExpression;
ConditionalOrExpression1(ConditionalOrExpression): ConditionalOrExpression(ConditionalOrExpression) OROR ConditionalAndExpression;
ConditionalExpression: ConditionalOrExpression ConditionalExpressiong1?;
ConditionalExpressiong2:
    ConditionalExpression
|   LambdaExpression
;
ConditionalExpressiong1: QUESTION Expression COLON ConditionalExpressiong2;
AssignmentExpression:
    ConditionalExpression
|   Assignment
;
Assignment: LeftHandSide AssignmentOperator Expression;
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
LambdaExpression: LambdaParameters ARROW LambdaBody;
LambdaParameters:
    LPAREN LambdaParameterList? RPAREN
|   Identifier
;
LambdaParameterList:
    LambdaParameter LambdaParameterListg1*
|   Identifier LambdaParameterListg2*
;
LambdaParameterListg2: COMMA Identifier;
LambdaParameterListg1: COMMA LambdaParameter;
LambdaParameter:
    VariableModifier* LambdaParameterType VariableDeclaratorId
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
SwitchExpression: SWITCH_KEYWORD LPAREN Expression RPAREN SwitchBlock;
ConstantExpression: Expression;