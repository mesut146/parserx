include "javaLexer.g"

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
  |TypeImportOnDemandDeclaration
  |SingleStaticImportDeclaration
  |StaticImportOnDemandDeclaration;

SingleTypeImportDeclaration:
  "import" TypeName;

TypeImportOnDemandDeclaration:
  "import" PackageOrTypeName "." "*";

SingleStaticImportDeclaration:
  "import" "static" TypeName . Identifier;

StaticImportOnDemandDeclaration:
  "import" "static" TypeName "." "*" ;

TopLevelClassOrInterfaceDeclaration:
  ClassDeclaration
  InterfaceDeclaration
;

ModuleDeclaration:
  Annotation* "open"? "module" Identifier (. Identifier)* /*{ ModuleDirective* }*/;

ModuleDirective:
  "requires" RequiresModifier* ModuleName ;
  "exports" PackageName ("to" ModuleName (, ModuleName)*)? ;
  "opens" PackageName ("to" ModuleName (, ModuleName)*) ;
  "uses" TypeName ;
  "provides" TypeName "with" TypeName (, TypeName)* ;

RequiresModifier:
  "transitive" | "static";


ClassDeclaration:
  NormalClassDeclaration
  EnumDeclaration
  RecordDeclaration;

NormalClassDeclaration:
  *ClassModifier* "class" TypeIdentifier TypeParameters? ClassExtends? ClassImplements? ClassBody;

ClassModifier:
  Annotation | "public" | "protected" | "private"
  "abstract" | "static" | "final" | "strictfp";

TypeParameters:
  "<" TypeParameterList ">";

TypeParameterList:
  TypeParameter (, TypeParameter)*;

TypeParameter:
  TypeParameterModifier* TypeIdentifier TypeBound?;

TypeParameterModifier:
  Annotation;

TypeBound:
  "extends" TypeVariable
  "extends" ClassOrInterfaceType AdditionalBound*;

AdditionalBound:
  "&" InterfaceType;

ClassExtends:
  "extends" ClassType;

ClassImplements:
  "implements" InterfaceTypeList;

InterfaceTypeList:
  InterfaceType ("," InterfaceType)*;

ClassBody:
{ ClassBodyDeclaration* };

ClassBodyDeclaration:
  ClassMemberDeclaration
  InstanceInitializer
  StaticInitializer
  ConstructorDeclaration;

ClassMemberDeclaration:
  FieldDeclaration
  MethodDeclaration
  ClassDeclaration
  InterfaceDeclaration
;

FieldDeclaration:
  FieldModifier* UnannType VariableDeclaratorList ;

VariableDeclaratorList:
  VariableDeclarator ("," VariableDeclarator)*;

VariableDeclarator:
  VariableDeclaratorId ("=" VariableInitializer)?;

VariableDeclaratorId:
  Identifier Dims?;

VariableInitializer:
  Expression
  ArrayInitializer;

UnannType:
  UnannPrimitiveType
  UnannReferenceType

UnannPrimitiveType:
  NumericType
  "boolean";

UnannReferenceType:
  UnannClassOrInterfaceType
  |UnannTypeVariable
  |UnannArrayType
  |UnannClassOrInterfaceType:
  |UnannClassType
  |UnannInterfaceType;

UnannClassType:
  TypeIdentifier TypeArguments?
  |PackageName "." Annotation* TypeIdentifier TypeArguments?
  |UnannClassOrInterfaceType "." Annotation* TypeIdentifier TypeArguments?;

UnannInterfaceType:
  UnannClassType;

UnannTypeVariable:
  TypeIdentifier;

UnannArrayType:
  UnannPrimitiveType Dims
  |UnannClassOrInterfaceType Dims
  |UnannTypeVariable Dims  ;

Dims:
  Annotation* "[" "]" (Annotation* "[" "]")*;

FieldModifier:
  Annotation | "public" "protected" | "private"
  |"static" | "final" | "transient" | "volatile";

MethodDeclaration:
  {MethodModifier} MethodHeader MethodBody;

MethodHeader:
  Result MethodDeclarator Throws?
 |TypeParameters Annotation* Result MethodDeclarator Throws?;

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
  UnannType
  |"void";

Throws:
  "throws" ExceptionTypeList;

ExceptionTypeList:
  ExceptionType ("," ExceptionType)*;

ExceptionType:
  ClassType
  |TypeVariable;


MethodBody:
  Block;