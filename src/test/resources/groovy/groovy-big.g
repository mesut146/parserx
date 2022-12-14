include "GroovyLexer.g"

nls
    :   NL*
    ;

sep :   (NL | SEMI)+
    ;

annotationsOpt:   (annotation (nls annotation)* nls)?;

annotation
    :   "@" qualifiedClassName (nls "(" elementValues? ")")?
    ;
elementValues
    :   elementValuePairs
    |   elementValue
    ;
elementValuePairs
    :   elementValuePair (COMMA elementValuePair)*
    ;

elementValuePair
    :   elementValuePairName nls ASSIGN nls elementValue
    ;

elementValuePairName
    :   identifier
    |   keywords
    ;
elementValue
    :   elementValueArrayInitializer
    |   annotation
    |   expression
    ;

elementValueArrayInitializer
    :   LBRACK (elementValue (COMMA elementValue)* COMMA?)? RBRACK
    ;


compilationUnit: nls (packageDeclaration sep?)? scriptStatements?;
packageDeclaration :   annotationsOpt "package" qualifiedName;
importDeclaration
    :   annotationsOpt IMPORT STATIC? qualifiedName (DOT MUL | AS alias=identifier)?
    ;
typeDeclaration
    :   classOrInterfaceModifiersOpt classDeclaration
    ;
classDeclaration: (   CLASS
                          |   INTERFACE
                          //|   ENUM
                          |   AT INTERFACE
                          |   TRAIT
                          |   RECORD
                          )
                          identifier
                          //(nls typeParameters)?
                          //(nls formalParameters)?
                          //(nls EXTENDS nls scs=typeList)?
                          //(nls IMPLEMENTS nls is=typeList)?
                          //(nls PERMITS nls ps=typeList)?
                          nls classBody;
enumDecl: ENUM identifier nls "{" enumConstants (nls COMMA)? sep? "}";
classBody
    :   LBRACE nls
        (classBodyDeclaration (sep classBodyDeclaration)*)? sep? RBRACE
    ;
enumConstants
    :   enumConstant (nls COMMA nls enumConstant)*
    ;

enumConstant
    :   annotationsOpt identifier arguments? anonymousInnerClassDeclaration?
    ;
anonymousInnerClassDeclaration: %empty;

classBodyDeclaration
    :   (STATIC nls)? block
    |   memberDeclaration
    ;
memberDeclaration: %empty;//todo

block
    :   LBRACE sep? blockStatements? RBRACE
    ;

blockStatement
    :   /*localVariableDeclaration
    |*/   statement
    ;
blockStatements
    :   blockStatement (sep blockStatement)* sep?
    ;
statement: %empty;

scriptStatements:   scriptStatement (sep scriptStatement)* sep?;

scriptStatement
    :   importDeclaration // Import statement.  Can be used in any scope.  Has "import x as y" also.
    |   typeDeclaration
    // validate the method in the AstBuilder#visitMethodDeclaration, e.g. method without method body is not allowed
    |   /*{ !SemanticPredicates::isInvalidMethodDeclaration(_input) }?*/
        methodDeclaration
    |   statement
    ;
methodDeclaration: methodName formalParameters   (
                                                          DEFAULT nls elementValue
                                                      |
                                                          (nls THROWS nls qualifiedClassNameList)?
                                                          (nls block)?
                                                      )?;
methodName
    :   identifier
    |   StringLiteral
    ;

returnType
    :
        standardType
    |   VOID
    ;
standardType
//options { baseContext = type; }
    :   annotationsOpt
        (
            primitiveType
        |
            standardClassOrInterfaceType
        )
        emptyDims?
    ;
variableInitializer
    :   enhancedStatementExpression
    ;

variableInitializers
    :   variableInitializer (nls COMMA nls variableInitializer)* nls COMMA?
    ;
emptyDims
    :   (annotationsOpt LBRACK RBRACK)+
    ;
enhancedStatementExpression
    :   statementExpression
    /*|   standardLambdaExpression*/
    ;

statementExpression
    :   commandExpression                   #commandExprAlt
    ;
type
    :   annotationsOpt
        (
            (
                primitiveType
            |
                // !!! Error Alternative !!!
                 VOID
            )
        |
                generalClassOrInterfaceType
        )
        emptyDims?
    ;
classOrInterfaceType
    :   (   qualifiedClassName
        |   qualifiedStandardClassName
        ) typeArguments?
    ;

generalClassOrInterfaceType
//options { baseContext = classOrInterfaceType; }
    :   qualifiedClassName typeArguments?
    ;

standardClassOrInterfaceType
//options { baseContext = classOrInterfaceType; }
    :   qualifiedStandardClassName typeArguments?
    ;
primitiveType
    :   BuiltInPrimitiveType
    ;
typeArguments
    :   LT nls typeArgument (COMMA nls typeArgument)* nls GT
    ;

typeArgument
    :   type
    |   annotationsOpt QUESTION ((EXTENDS | SUPER) nls type)?
    ;
annotatedQualifiedClassName
    :   annotationsOpt qualifiedClassName
    ;
qualifiedClassNameList
    :   annotatedQualifiedClassName (COMMA nls annotatedQualifiedClassName)*
    ;

formalParameters
    :   "(" formalParameterList? ")"
    ;

formalParameterList
    :   (formalParameter | thisFormalParameter) (COMMA nls formalParameter)*
    ;

thisFormalParameter
    :   type THIS
    ;

formalParameter
    :   variableModifiersOpt type? ELLIPSIS? identifier (nls ASSIGN nls expression)?
    ;

literal
    :   IntegerLiteral                                                                      #integerLiteralAlt
    |   FloatingPointLiteral                                                                #floatingPointLiteralAlt
    |   StringLiteral                                                                       #stringLiteralAlt
    |   BooleanLiteral                                                                      #booleanLiteralAlt
    |   NullLiteral                                                                         #nullLiteralAlt
    ;

expression: %empty;

typeArgumentsOrDiamond
    :   LT GT
    |   typeArguments
    ;

arguments
    :   "(" enhancedArgumentListInPar? ","? ")"
    ;
enhancedArgumentListInPar
    :   enhancedArgumentListElement
        (   COMMA nls
            enhancedArgumentListElement
        )*
    ;
firstArgumentListElement
//options { baseContext = enhancedArgumentListElement; }
    :   expressionListElement
    |   namedArg
    ;
argumentListElement
//options { baseContext = enhancedArgumentListElement; }
    :   expressionListElement[true]
    |   namedPropertyArg
    ;

enhancedArgumentListElement
    :   expressionListElement
    |   standardLambdaExpression
    |   namedPropertyArg
    ;
standardLambdaExpression: %empty;
classOrInterfaceModifiersOpt
    :   (classOrInterfaceModifiers
            NL* /* Use `NL*` here for better performance, so DON'T replace it with `nls` */
        )?
    ;
classOrInterfaceModifiers
    :   classOrInterfaceModifier (nls classOrInterfaceModifier)*
    ;
classOrInterfaceModifier
    :   annotation       // class or interface
    |   m=(   PUBLIC     // class or interface
          |   PROTECTED  // class or interface
          |   PRIVATE    // class or interface
          |   STATIC     // class or interface
          |   ABSTRACT   // class or interface
          |   SEALED     // class or interface
          |   NON_SEALED // class or interface
          |   FINAL      // class only -- does not apply to interfaces
          |   STRICTFP   // class or interface
          |   DEFAULT    // interface only -- does not apply to classes
          )
    ;
variableModifier
    :   annotation
    |   m=( FINAL
          | DEF
          | VAR
          // Groovy supports declaring local variables as instance/class fields,
          // e.g. import groovy.transform.*; @Field static List awe = [1, 2, 3]
          // e.g. import groovy.transform.*; def a = { @Field public List awe = [1, 2, 3] }
          // Notice: Groovy 2.4.7 just allows to declare local variables with the following modifiers when using annotations(e.g. @Field)
          // TODO check whether the following modifiers accompany annotations or not. Because the legacy codes(e.g. benchmark/bench/heapsort.groovy) allow to declare the special instance/class fields without annotations, we leave it as it is for the time being
          | PUBLIC
          | PROTECTED
          | PRIVATE
          | STATIC
          | ABSTRACT
          | STRICTFP
          )
    ;

variableModifiersOpt
    :   (variableModifiers nls)?
    ;

variableModifiers
    :   variableModifier (nls variableModifier)*
    ;
qualifiedName
    :   qualifiedNameElement (DOT qualifiedNameElement)*
    ;
qualifiedNameElement
    :   identifier
    |   DEF
    |   IN
    |   AS
    |   TRAIT
    ;

qualifiedNameElements
    :   (qualifiedNameElement DOT)*
    ;

qualifiedClassName
    :   qualifiedNameElements identifier
    ;

qualifiedStandardClassName
    :   qualifiedNameElements className (DOT className)*
    ;
className:   CapitalizedIdentifier;
identifier
    :   Identifier
    |   CapitalizedIdentifier
    |   VAR
    |   IN
//  |   DEF
    |   TRAIT
    |   AS
    |   YIELD
    |   PERMITS
    |   SEALED
    |   RECORD
    ;
keywords
    :   ABSTRACT
    |   AS
    |   ASSERT
    |   BREAK
    |   CASE
    |   CATCH
    |   CLASS
    |   CONST
    |   CONTINUE
    |   DEF
    |   DEFAULT
    |   DO
    |   ELSE
    |   ENUM
    |   EXTENDS
    |   FINAL
    |   FINALLY
    |   FOR
    |   GOTO
    |   IF
    |   IMPLEMENTS
    |   IMPORT
    |   IN
    |   INSTANCEOF
    |   INTERFACE
    |   NATIVE
    |   NEW
    |   NON_SEALED
    |   PACKAGE
    |   PERMITS
    |   RECORD
    |   RETURN
    |   SEALED
    |   STATIC
    |   STRICTFP
    |   SUPER
    |   SWITCH
    |   SYNCHRONIZED
    |   THIS
    |   THROW
    |   THROWS
    |   TRANSIENT
    |   TRAIT
    |   THREADSAFE
    |   TRY
    |   VAR
    |   VOLATILE
    |   WHILE
    |   YIELD

    |   NullLiteral
    |   BooleanLiteral

    |   BuiltInPrimitiveType
    |   VOID

    |   PUBLIC
    |   PROTECTED
    |   PRIVATE
    ;

expressionList
    :   expressionListElement (COMMA nls expressionListElement)*
    ;

expressionListElement
    :   MUL? expression
    ;
map
    :   LBRACK
        (   mapEntryList COMMA?
        |   COLON
        )
        RBRACK
    ;

mapEntryList
    :   mapEntry (COMMA mapEntry)*
    ;

namedPropertyArgList
//options { baseContext = mapEntryList; }
    :   namedPropertyArg (COMMA namedPropertyArg)*
    ;

mapEntry
    :   mapEntryLabel COLON nls expression
    |   MUL COLON nls expression
    ;

namedPropertyArg
//options { baseContext = mapEntry; }
    :   namedPropertyArgLabel COLON nls expression
    |   MUL COLON nls expression
    ;

namedArg
//options { baseContext = mapEntry; }
    :   namedArgLabel COLON nls expression
    |   MUL COLON nls expression
    ;

mapEntryLabel
    :   keywords
    |   primary
    ;

namedPropertyArgLabel
//options { baseContext = mapEntryLabel; }
    :   keywords
    |   namedPropertyArgPrimary
    ;

namedArgLabel
//options { baseContext = mapEntryLabel; }
    :   keywords
    |   namedArgPrimary
    ;
creator
    :   createdName
        (   nls arguments anonymousInnerClassDeclaration?
        |   dim+ (nls arrayInitializer)?
        )
    ;
createdName
    :   annotationsOpt
        (   primitiveType
        |   qualifiedClassName typeArgumentsOrDiamond?
        )
    ;

dim
    :   annotationsOpt LBRACK expression? RBRACK
    ;
arrayInitializer
    :   LBRACE nls (variableInitializers nls)? RBRACE
    ;
primary
    :
        // Append `typeArguments?` to `identifier` to support constructor reference with generics, e.g. HashMap<String, Integer>::new
        // Though this is not a graceful solution, it is much faster than replacing `builtInType` with `type`
        identifier typeArguments?                                                           #identifierPrmrAlt
    |   literal                                                                             #literalPrmrAlt
    |   gstring                                                                             #gstringPrmrAlt
    |   NEW nls creator                                                                 #newPrmrAlt
    |   THIS                                                                                #thisPrmrAlt
    |   SUPER                                                                               #superPrmrAlt
    |   parExpression                                                                       #parenPrmrAlt
    |   closureOrLambdaExpression                                                           #closureOrLambdaExpressionPrmrAlt
    |   list                                                                                #listPrmrAlt
    |   map                                                                                 #mapPrmrAlt
    |   builtInType                                                                         #builtInTypePrmrAlt
    ;
namedPropertyArgPrimary
//options { baseContext = primary; }
    :   identifier                                                                          #identifierPrmrAlt_namedPropertyArgPrimary
    |   literal                                                                             #literalPrmrAlt_namedPropertyArgPrimary
    |   gstring                                                                             #gstringPrmrAlt_namedPropertyArgPrimary
    |   parExpression                                                                       #parenPrmrAlt_namedPropertyArgPrimary
    |   list                                                                                #listPrmrAlt_namedPropertyArgPrimary
    |   map                                                                                 #mapPrmrAlt_namedPropertyArgPrimary
    ;
namedArgPrimary
//options { baseContext = primary; }
    :   identifier                                                                          #identifierPrmrAlt_namedArgPrimary
    |   literal                                                                             #literalPrmrAlt_namedArgPrimary
    |   gstring                                                                             #gstringPrmrAlt_namedArgPrimary
    ;

commandPrimary
//options { baseContext = primary; }
    :   identifier                                                                          #identifierPrmrAlt_commandPrimary
    |   literal                                                                             #literalPrmrAlt_commandPrimary
    |   gstring                                                                             #gstringPrmrAlt_commandPrimary
    ;

list
    :   LBRACK expressionList? COMMA? RBRACK
    ;
gstring: GString;