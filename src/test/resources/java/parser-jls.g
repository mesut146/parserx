include "lexer-jls.g"
//https://docs.oracle.com/javase/specs/jls/se16/html/jls-15.html


//3.8. Identifiers
TypeIdentifier:
  Identifier; /*but not var, yield, or record*/

UnqualifiedMethodIdentifier:
  Identifier | "var" | "record"; /*but not yield*/

//3.10. Literals
Literal:
   IntegerLiteral
 | FloatingPointLiteral
 | BooleanLiteral
 | CharacterLiteral
 | StringLiteral
 | TextBlock
 | NullLiteral;


//4.1. The Kinds of Types and Values
Type:
  PrimitiveType | ReferenceType;

//4.2. Primitive Types and Values
PrimitiveType:
   Annotation* (NumericType | "boolean");

NumericType:
  IntegralType | FloatingPointType;

IntegralType:
  "byte" | "short" | "int" | "long" | "char";

FloatingPointType:
  "float" | "double";

//4.3. Reference Types and Values
ReferenceType:
   ClassOrInterfaceType
 | TypeVariable
 | ArrayType;

ClassOrInterfaceType:
  ClassType | InterfaceType;

ClassType:
   Annotation* TypeIdentifier TypeArguments?
 | PackageName "." Annotation* TypeIdentifier TypeArguments?
 | ClassOrInterfaceType "." Annotation* TypeIdentifier TypeArguments?;

InterfaceType:
   ClassType;

TypeVariable:
  Annotation* TypeIdentifier;

ArrayType:
   PrimitiveType Dims
 | ClassOrInterfaceType Dims
 | TypeVariable Dims;

Dims:
  Annotation* "[" "]" (Annotation* "[" "]")*;


//4.4. Type Variables
TypeParameter:
  TypeParameterModifier* TypeIdentifier TypeBound?;

TypeParameterModifier:
  Annotation;

TypeBound:
   "extends" (TypeVariable | ClassOrInterfaceType AdditionalBound*);

AdditionalBound:
  "&" InterfaceType;

//4.5.1. Type Arguments of Parameterized Types
TypeArguments:
   "<" TypeArgumentList ">";

TypeArgumentList:
  TypeArgument ("," TypeArgument)*;

TypeArgument:
  ReferenceType | Wildcard;

Wildcard:
  Annotation* "?" WildcardBounds?;

WildcardBounds:
   "extends" ReferenceType | "super" ReferenceType;

CompilationUnit:
  OrdinaryCompilationUnit | ModularCompilationUnit;

OrdinaryCompilationUnit:
  PackageDeclaration? ImportDeclaration* TopLevelClassOrInterfaceDeclaration*;

ModularCompilationUnit:
  ImportDeclaration* ModuleDeclaration;

PackageDeclaration:
  PackageModifier* "package" Identifier ("." Identifier)*;

PackageModifier:
  Annotation;

ImportDeclaration:
   SingleTypeImportDeclaration
 | TypeImportOnDemandDeclaration
 | SingleStaticImportDeclaration
 | StaticImportOnDemandDeclaration;

SingleTypeImportDeclaration:
  "import" TypeName;

TypeImportOnDemandDeclaration:
  "import" PackageOrTypeName "." "*";

SingleStaticImportDeclaration:
  "import" "static" TypeName "." Identifier;

StaticImportOnDemandDeclaration:
  "import" "static" TypeName "." "*" ;

TopLevelClassOrInterfaceDeclaration:
   ClassDeclaration
 | InterfaceDeclaration;

//6.5. Determining the Meaning of a Name
ModuleName:
   Identifier
 | ModuleName "." Identifier;

PackageName:
   Identifier
 | PackageName "." Identifier;

TypeName:
   TypeIdentifier
 | PackageOrTypeName "." TypeIdentifier;

PackageOrTypeName:
   Identifier
 | PackageOrTypeName "." Identifier;

ExpressionName:
  Identifier
 | AmbiguousName "." Identifier;

MethodName:
  UnqualifiedMethodIdentifier;

AmbiguousName:
   Identifier
 | AmbiguousName "." Identifier;



ModuleDeclaration:
  Annotation* "open"? "module" Identifier ("." Identifier)* /*{ ModuleDirective* }*/;

//9.7
Annotation:
   NormalAnnotation
 | MarkerAnnotation
 | SingleElementAnnotation;

//9.7.1
NormalAnnotation:
  "@" TypeName "(" ElementValuePairList? ")";

ElementValuePairList:
  ElementValuePair ("," ElementValuePair)*;

ElementValuePair:
  Identifier "=" ElementValue;

ElementValue:
   ConditionalExpression
 | ElementValueArrayInitializer
 | Annotation;

ElementValueArrayInitializer:
  ( ElementValueList? ","? )*;

ElementValueList:
  ElementValue ("," ElementValue)*;

//9.7.2
MarkerAnnotation:
  "@" TypeName;

//9.7.3
SingleElementAnnotation:
  "@" TypeName "(" ElementValue ")"  ;

ModuleDirective:
   "requires" RequiresModifier* ModuleName
 | "exports" PackageName ("to" ModuleName ("," ModuleName)*)?
 | "opens" PackageName ("to" ModuleName ("," ModuleName)*)?
 | "uses" TypeName
 | "provides" TypeName "with" TypeName ("," TypeName)* ;

RequiresModifier:
  "transitive" | "static";

//8.1. Class Declarations
ClassDeclaration:
   NormalClassDeclaration
 | EnumDeclaration
 | RecordDeclaration;

NormalClassDeclaration:
  ClassModifier* "class" TypeIdentifier TypeParameters? ClassExtends? ClassImplements? ClassBody;

//8.1.1. Class Modifiers
ClassModifier:
  Annotation | "public" | "protected" | "private"
  "abstract" | "static" | "final" | "strictfp";

//8.8. Constructor Declarations
ConstructorDeclaration:
  ConstructorModifier* ConstructorDeclarator Throws? ConstructorBody;

ConstructorDeclarator:
  TypeParameters? SimpleTypeName "(" (ReceiverParameter ",")? FormalParameterList? ")";

SimpleTypeName:
  TypeIdentifier;

//8.8.3. Constructor Modifiers
ConstructorModifier:
  Annotation | "public" | "protected" | "private";

//8.8.7. Constructor Body
ConstructorBody:
  "{" ExplicitConstructorInvocation? BlockStatements? "}";

//8.8.7.1. Explicit Constructor Invocations
ExplicitConstructorInvocation:
   TypeArguments? "this" "(" ArgumentList? ")" ";"
 | TypeArguments? "super" "(" ArgumentList? ")" ";"
 | ExpressionName "." TypeArguments? "super" "(" ArgumentList? ")" ";"
 | Primary "." TypeArguments? "super" "(" ArgumentList? ")" ";";


//8.9. Enum Classes
EnumDeclaration:
  ClassModifier* "enum" TypeIdentifier ClassImplements? EnumBody;

//8.9.1. Enum Constants
EnumBody:
  "{" EnumConstantList? ","? EnumBodyDeclarations? "}";

EnumConstantList:
  EnumConstant ("," EnumConstant)*;

EnumConstant:
  EnumConstantModifier* Identifier ("(" ArgumentList? ")")? ClassBody?;

EnumConstantModifier:
  Annotation;

//8.9.2. Enum Body Declarations
EnumBodyDeclarations:
  ";" ClassBodyDeclaration*;

//8.10. Record Classes
RecordDeclaration:
  ClassModifier* "record" TypeIdentifier TypeParameters? RecordHeader ClassImplements? RecordBody;

//8.10.1. Record Components
RecordHeader:
  "(" RecordComponentList? ")";

RecordComponentList:
  RecordComponent ("," RecordComponent)*;

RecordComponent:
   RecordComponentModifier* UnannType Identifier
 | VariableArityRecordComponent;

VariableArityRecordComponent:
  RecordComponentModifier* UnannType Annotation* "..." Identifier;

RecordComponentModifier:
  Annotation;

//8.10.2. Record Body Declarations
RecordBody:
  "{" RecordBodyDeclaration* "}";

RecordBodyDeclaration:
  ClassBodyDeclaration | CompactConstructorDeclaration;

//8.10.4.2. Compact Canonical Constructors
CompactConstructorDeclaration:
  ConstructorModifier* SimpleTypeName ConstructorBody;

//9.1.2. Generic Interfaces and Type Parameters
TypeParameters:
  "<" TypeParameterList ">";

TypeParameterList:
  TypeParameter ("," TypeParameter)*;

ClassExtends:
  "extends" ClassType;

ClassImplements:
  "implements" InterfaceTypeList;

InterfaceTypeList:
  InterfaceType ("," InterfaceType)*;

//8.1.6. Class Body and Member Declarations
ClassBody:
  "{" ClassBodyDeclaration* "}";

ClassBodyDeclaration:
   ClassMemberDeclaration
 | InstanceInitializer
 | StaticInitializer
 | ConstructorDeclaration;

//8.6. Instance Initializers
InstanceInitializer:
  Block;

//8.7. Static Initializers
StaticInitializer:
  "static" Block;

ClassMemberDeclaration:
   FieldDeclaration
 | MethodDeclaration
 | ClassDeclaration
 | InterfaceDeclaration;

//9.1. Interface Declarations
InterfaceDeclaration:
   NormalInterfaceDeclaration
 | AnnotationInterfaceDeclaration;

NormalInterfaceDeclaration:
  InterfaceModifier* "interface" TypeIdentifier TypeParameters? InterfaceExtends? InterfaceBody;

//9.1.1. Interface Modifiers
InterfaceModifier:
   Annotation | "public" | "protected" | "private"
 | "abstract" | "static" | "strictfp";

//9.1.3. Superinterfaces and Subinterfaces
InterfaceExtends:
  "extends" InterfaceTypeList;

//9.1.4. Interface Body and Member Declarations
InterfaceBody:
  "{" InterfaceMemberDeclaration* "}";

InterfaceMemberDeclaration:
   ConstantDeclaration
 | InterfaceMethodDeclaration
 | ClassDeclaration
 | InterfaceDeclaration
 | ";";

//9.3. Field (Constant) Declarations
ConstantDeclaration:
  ConstantModifier* UnannType VariableDeclaratorList ";";

ConstantModifier:
   Annotation | "public" | "static" | "final";

//9.4. Method Declarations
InterfaceMethodDeclaration:
  InterfaceMethodModifier* MethodHeader MethodBody;

InterfaceMethodModifier:
  Annotation | "public" | "private" | "abstract" | "default" | "static" | "strictfp";


//9.6. Annotation Interfaces
AnnotationInterfaceDeclaration:
  InterfaceModifier* "@" "interface" TypeIdentifier AnnotationInterfaceBody;

//9.6.1. Annotation Interface Elements
AnnotationInterfaceBody:
  "{" AnnotationInterfaceMemberDeclaration* "}";

AnnotationInterfaceMemberDeclaration:
   AnnotationInterfaceElementDeclaration
 | ConstantDeclaration
 | ClassDeclaration
 | InterfaceDeclaration
 | ";";

AnnotationInterfaceElementDeclaration:
  AnnotationInterfaceElementModifier* UnannType Identifier "(" ")" Dims? DefaultValue? ";";

AnnotationInterfaceElementModifier:
  Annotation | "public" | "abstract";

//9.6.2. Defaults for Annotation Interface Elements
DefaultValue:
  "default" ElementValue;

//10.6. Array Initializers
ArrayInitializer:
  "{" VariableInitializerList? ","? "}";

VariableInitializerList:
  VariableInitializer ("," VariableInitializer)*;

FieldDeclaration:
  FieldModifier* UnannType VariableDeclaratorList ;

VariableDeclaratorList:
  VariableDeclarator ("," VariableDeclarator)*;

VariableDeclarator:
  VariableDeclaratorId ("=" VariableInitializer)?;

VariableDeclaratorId:
  Identifier Dims?;

VariableInitializer:
  Expression | ArrayInitializer;

UnannType:
  UnannPrimitiveType | UnannReferenceType;

UnannPrimitiveType:
  NumericType | "boolean";

UnannReferenceType:
   UnannClassOrInterfaceType
 | UnannTypeVariable
 | UnannArrayType;

UnannClassOrInterfaceType:
   UnannClassType
 | UnannInterfaceType;

UnannClassType:
   TypeIdentifier TypeArguments?
 | PackageName "." Annotation* TypeIdentifier TypeArguments?
 | UnannClassOrInterfaceType "." Annotation* TypeIdentifier TypeArguments?;

UnannInterfaceType:
  UnannClassType;

UnannTypeVariable:
  TypeIdentifier;

UnannArrayType:
   UnannPrimitiveType Dims
 | UnannClassOrInterfaceType Dims
 | UnannTypeVariable Dims  ;


FieldModifier:
  Annotation | "public" "protected" | "private"
  |"static" | "final" | "transient" | "volatile";

MethodDeclaration:
  MethodModifier* MethodHeader MethodBody;

MethodHeader:
   Result MethodDeclarator Throws?
 | TypeParameters Annotation* Result MethodDeclarator Throws?;

MethodDeclarator:
  Identifier "(" (ReceiverParameter ",")? FormalParameterList? ")" Dims?;

ReceiverParameter:
  Annotation* UnannType (Identifier ".")? "this";

FormalParameterList:
  FormalParameter ("," FormalParameter)*;

FormalParameter:
  VariableModifier* UnannType VariableDeclaratorId
  | VariableArityParameter;

VariableArityParameter:
  VariableModifier* UnannType Annotation* "..." Identifier;

VariableModifier:
  Annotation | "final";

MethodModifier:
  Annotation | "public" | "protected" | "private"
  | "abstract" | "static" | "final" | "synchronized" | "native" | "strictfp";

Result:
  UnannType | "void";

Throws:
  "throws" ExceptionTypeList;

ExceptionTypeList:
  ExceptionType ("," ExceptionType)*;

ExceptionType:
   ClassType | TypeVariable;

MethodBody:
  Block;

//14.2. Blocks
Block:
   "{" BlockStatements? "}";

BlockStatements:
  BlockStatement BlockStatement*;

BlockStatement:
   LocalClassOrInterfaceDeclaration
 | LocalVariableDeclarationStatement
 | Statement;

//14.3. Local Class and Interface Declarations
LocalClassOrInterfaceDeclaration:
  ClassDeclaration | NormalInterfaceDeclaration;

//14.4. Local Variable Declarations
LocalVariableDeclaration:
  VariableModifier* LocalVariableType VariableDeclaratorList;

LocalVariableType:
  UnannType | "var";

//14.4.2. Local Variable Declaration Statements
LocalVariableDeclarationStatement:
  LocalVariableDeclaration ";";

//14.5. Statements
Statement:
   StatementWithoutTrailingSubstatement
 | LabeledStatement
 | IfThenStatement
 | IfThenElseStatement
 | WhileStatement
 | ForStatement;

StatementNoShortIf:
   StatementWithoutTrailingSubstatement
 | LabeledStatementNoShortIf
 | IfThenElseStatementNoShortIf
 | WhileStatementNoShortIf
 | ForStatementNoShortIf;

StatementWithoutTrailingSubstatement:
   Block
 | EmptyStatement
 | ExpressionStatement
 | AssertStatement
 | SwitchStatement
 | DoStatement
 | BreakStatement
 | ContinueStatement
 | ReturnStatement
 | SynchronizedStatement
 | ThrowStatement
 | TryStatement
 | YieldStatement;

//14.6. The Empty Statement
EmptyStatement: ";";

//14.7. Labeled Statements
LabeledStatement:
  Identifier ":" Statement;
LabeledStatementNoShortIf:
  Identifier ":" StatementNoShortIf;

//14.8. Expression Statements
ExpressionStatement:
  StatementExpression ";";

StatementExpression:
   Assignment
 | PreIncrementExpression
 | PreDecrementExpression
 | PostIncrementExpression
 | PostDecrementExpression
 | MethodInvocation
 | ClassInstanceCreationExpression;

//14.9. The if Statement
IfThenStatement:
  "if" "(" Expression ")" Statement;

IfThenElseStatement:
  "if" "(" Expression ")" StatementNoShortIf "else" Statement;

IfThenElseStatementNoShortIf:
  "if" "(" Expression ")" StatementNoShortIf "else" StatementNoShortIf;

//14.10. The assert Statement
AssertStatement:
  "assert" Expression (":" Expression)? ";";

//14.11. The switch Statement
SwitchStatement:
  "switch" "(" Expression ")" SwitchBlock;

//14.11.1. Switch Blocks
SwitchBlock:
   "{" SwitchRule SwitchRule* "}"
 | "{" SwitchBlockStatementGroup* (SwitchLabel ":")* "}";

SwitchRule:
  SwitchLabel "->" (Expression ";" | Block | ThrowStatement);

SwitchBlockStatementGroup:
  SwitchLabel ":" (SwitchLabel ":")* BlockStatements;

SwitchLabel:
   "case" CaseConstant ("," CaseConstant)*
 | "default";

CaseConstant:
  ConditionalExpression;

//14.12. The while Statement
WhileStatement:
  "while" "(" Expression ")" Statement;

WhileStatementNoShortIf:
  "while" "(" Expression ")" StatementNoShortIf;

//14.13. The do Statement
DoStatement:
  "do" Statement "while" "(" Expression ")" ";";

//14.14. The for Statement
ForStatement:
  BasicForStatement | EnhancedForStatement;

ForStatementNoShortIf:
  BasicForStatementNoShortIf | EnhancedForStatementNoShortIf;

//14.14.1. The basic for Statement
BasicForStatement:
  "for" "(" ForInit? ";" Expression? ";" ForUpdate? ")" Statement;

BasicForStatementNoShortIf:
  "for" "(" ForInit? ";" Expression? ";" ForUpdate? ")" StatementNoShortIf;

ForInit:
  StatementExpressionList | LocalVariableDeclaration;

ForUpdate:
   StatementExpressionList;

StatementExpressionList:
  StatementExpression ("," StatementExpression)*;

//14.14.2. The enhanced for statement
EnhancedForStatement:
  "for" "(" LocalVariableDeclaration ":" Expression ")" Statement;

EnhancedForStatementNoShortIf:
  "for" "(" LocalVariableDeclaration ":" Expression ")" StatementNoShortIf;

//14.15. The break Statement
BreakStatement:  "break" Identifier? ";";

//14.16. The continue Statement
ContinueStatement:
  "continue" Identifier? ";";

//14.17. The return Statement
ReturnStatement:
  "return" Expression? ";";

//14.18. The throw Statement
ThrowStatement:
  "throw" Expression ";";

//14.19. The synchronized Statement
SynchronizedStatement:
  "synchronized" "(" Expression ")" Block;

//14.20. The try statement
TryStatement:
   "try" Block (Catches Finally? | Finally)
 | TryWithResourcesStatement;

Catches:
  CatchClause CatchClause*;

CatchClause:
  "catch" "(" CatchFormalParameter ")" Block;

CatchFormalParameter:
  VariableModifier* CatchType VariableDeclaratorId;

CatchType:
  UnannClassType ("|" ClassType)*;

Finally:
  "finally" Block;

//14.20.3. try-with-resources
TryWithResourcesStatement:
  "try" ResourceSpecification Block Catches? Finally?;

ResourceSpecification:
  "(" ResourceList ";"? ")";

ResourceList:
  Resource (";" Resource)*;

Resource:
  LocalVariableDeclaration | VariableAccess;

VariableAccess:
  ExpressionName | FieldAccess;

//14.21. The yield Statement
YieldStatement:
  "yield" Expression ";";


//15.2. Forms of Expressions
Expression:
  LambdaExpression | AssignmentExpression;


//15.8. Primary Expressions
Primary:
  PrimaryNoNewArray | ArrayCreationExpression;

PrimaryNoNewArray:
   Literal
 | ClassLiteral
 | "this"
 | TypeName "." "this"
 | "(" Expression ")"
 | ClassInstanceCreationExpression
 | FieldAccess
 | ArrayAccess
 | MethodInvocation
 | MethodReference;

//15.8.1. Lexical Literals

//15.8.2. Class Literals
ClassLiteral:
   TypeName ("[" "]")* "." "class"
 | NumericType ("[" "]")* "." "class"
 | "boolean" ("[" "]")* "." "class"
 | "void" "." "class";


//15.9. Class Instance Creation Expressions
ClassInstanceCreationExpression:
   UnqualifiedClassInstanceCreationExpression
 | ExpressionName "." UnqualifiedClassInstanceCreationExpression
 | Primary "." UnqualifiedClassInstanceCreationExpression;

UnqualifiedClassInstanceCreationExpression:
  "new" TypeArguments? ClassOrInterfaceTypeToInstantiate "(" ArgumentList? ")" ClassBody?;

ClassOrInterfaceTypeToInstantiate:
  Annotation* Identifier ("." Annotation* Identifier)* TypeArgumentsOrDiamond?;

TypeArgumentsOrDiamond:
  TypeArguments | "<" ">";

//15.10.1. Array Creation Expressions
ArrayCreationExpression:
  "new" (PrimitiveType | ClassOrInterfaceType) (DimExprs Dims? | Dims ArrayInitializer);

DimExprs:
  DimExpr DimExpr*;

DimExpr:
  Annotation*  Expression?;

//15.10.3. Array Access Expressions
ArrayAccess:
   ExpressionName Expression?
 | PrimaryNoNewArray Expression?;

//15.11. Field Access Expressions
FieldAccess:
 (Primary | "super" | TypeName "." "super") "." Identifier;


//15.12. Method Invocation Expressions
MethodInvocation:
   MethodName "(" ArgumentList? ")"
 | TypeName "." TypeArguments? Identifier "(" ArgumentList? ")"
 | ExpressionName "." TypeArguments? Identifier "(" ArgumentList? ")"
 | Primary "." TypeArguments? Identifier "(" ArgumentList? ")"
 | "super" "." TypeArguments? Identifier "(" ?ArgumentList? ")"
 | TypeName "." "super" "." TypeArguments? Identifier "(" ArgumentList? ")";

ArgumentList:
  Expression ("," Expression)*;

//15.13. Method Reference Expressions
MethodReference:
   ExpressionName "::" TypeArguments? Identifier
 | Primary "::" TypeArguments? Identifier
 | ReferenceType "::" TypeArguments? Identifier
 | "super" "::" TypeArguments? Identifier
 | TypeName "." "super" "::" TypeArguments? Identifier
 | ClassType "::" TypeArguments? "new"
 | ArrayType "::" "new";

//15.14. Postfix Expressions
PostfixExpression:
   Primary
 | ExpressionName
 | PostIncrementExpression
 | PostDecrementExpression;

//15.14.2. Postfix Increment Operator ++
PostIncrementExpression:
  PostfixExpression "++";

//15.14.3. Postfix Decrement Operator ++
PostDecrementExpression:
  PostfixExpression "--";

//15.15. Unary Operators

UnaryExpression:
   PreIncrementExpression
 | PreDecrementExpression
 | "+" UnaryExpression
 | "-" UnaryExpression
 | UnaryExpressionNotPlusMinus;

PreIncrementExpression: "++" UnaryExpression;

PreDecrementExpression: "--" UnaryExpression;

UnaryExpressionNotPlusMinus:
   PostfixExpression
 | "~" UnaryExpression
 | "!" UnaryExpression
 | CastExpression
 | SwitchExpression;

//15.16. Cast Expressions
CastExpression:
   "(" PrimitiveType ")" UnaryExpression
 | "(" ReferenceType AdditionalBound* ")" UnaryExpressionNotPlusMinus
 | "(" ReferenceType AdditionalBound* ")" LambdaExpression;

//15.17. Multiplicative Operators
MultiplicativeExpression:
   UnaryExpression
 | MultiplicativeExpression ("*" | "/" | "%") UnaryExpression;

//15.17.2. Division Operator /
//15.17.3. Remainder Operator %

//15.18. Additive Operators
AdditiveExpression:
   MultiplicativeExpression
 | AdditiveExpression ("+" | "-") MultiplicativeExpression;


//15.18.1. String Concatenation Operator +
//15.18.2. Additive Operators (+ and -) for Numeric Types

//15.19. Shift Operators
ShiftExpression:
   AdditiveExpression
 | ShiftExpression ("<<" | ">>" | ">>>") AdditiveExpression;


//15.20. Relational Operators
RelationalExpression:
   ShiftExpression
 | RelationalExpression ("<" | ">" | "<=" | ">=") ShiftExpression
 | RelationalExpression "instanceof" ReferenceType;

//15.20.1. Numerical Comparison Operators <, <=, >, and >=
//15.20.2. Type Comparison Operator instanceof
//15.21. Equality Operators
EqualityExpression:
   RelationalExpression
 | EqualityExpression ("==" | "!=") RelationalExpression;

//15.21.1. Numerical Equality Operators == and !=
//15.21.2. Boolean Equality Operators == and !=
//15.21.3. Reference Equality Operators == and !=

//15.22. Bitwise and Logical Operators
AndExpression:
   EqualityExpression
 | AndExpression "&" EqualityExpression;

ExclusiveOrExpression:
   AndExpression
 | ExclusiveOrExpression "^" AndExpression;

InclusiveOrExpression:
   ExclusiveOrExpression
 | InclusiveOrExpression "|" ExclusiveOrExpression;


//15.22.1. Integer Bitwise Operators &, ^, and |
//15.22.2. Boolean Logical Operators &, ^, and |
//15.23. Conditional-And Operator &&
ConditionalAndExpression:
   InclusiveOrExpression
 | ConditionalAndExpression "&&" InclusiveOrExpression;

//15.24. Conditional-Or Operator ||
ConditionalOrExpression:
   ConditionalAndExpression
 | ConditionalOrExpression "||" ConditionalAndExpression;

//15.25. Conditional Operator ? :
ConditionalExpression:
  ConditionalOrExpression ("?" Expression ":" (ConditionalExpression | LambdaExpression))?;

//15.26. Assignment Operators
AssignmentExpression:
  ConditionalExpression | Assignment;

Assignment:
  LeftHandSide AssignmentOperator Expression;

LeftHandSide:
   ExpressionName
 | FieldAccess
 | ArrayAccess;

AssignmentOperator:
  "=" | "*=" | "/=" | "%=" | "+=" | "-=" | "<<=" | ">>=" | ">>>=" | "&=" | "^=" | "|=";

//15.27. Lambda Expressions
LambdaExpression:
  LambdaParameters "->" LambdaBody;

//15.27.1. Lambda Parameters

LambdaParameters:
   "(" LambdaParameterList? ")"
 | Identifier;

LambdaParameterList:
   LambdaParameter ("," LambdaParameter)*
 | Identifier ("," Identifier)*;

LambdaParameter:
   VariableModifier* LambdaParameterType VariableDeclaratorId
 | VariableArityParameter;

LambdaParameterType:
  UnannType | "var";

//15.27.2. Lambda Body
LambdaBody:
  Expression | Block;

//15.28. switch Expressions
SwitchExpression:
  "switch" "(" Expression ")" SwitchBlock;

//15.29. Constant Expressions
ConstantExpression:
  Expression;
