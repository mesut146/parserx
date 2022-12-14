token{
// Groovy keywords
AS              : 'as';
DEF             : 'def';
IN              : 'in';
TRAIT           : 'trait';
THREADSAFE      : 'threadsafe'; // reserved keyword

// the reserved type name of Java10
VAR             : 'var';

// §3.9 Keywords
BuiltInPrimitiveType
    :   BOOLEAN
    |   CHAR
    |   BYTE
    |   SHORT
    |   INT
    |   LONG
    |   FLOAT
    |   DOUBLE
    ;
ABSTRACT      : 'abstract';
ASSERT        : 'assert';


#BOOLEAN       : 'boolean';

BREAK         : 'break';
YIELD         : 'yield';


#BYTE          : 'byte';

CASE          : 'case';
CATCH         : 'catch';


#CHAR          : 'char';

CLASS         : 'class';
CONST         : 'const';
CONTINUE      : 'continue';
DEFAULT       : 'default';
DO            : 'do';


#DOUBLE        : 'double';

ELSE          : 'else';
ENUM          : 'enum';
EXTENDS       : 'extends';
FINAL         : 'final';
FINALLY       : 'finally';


#FLOAT         : 'float';


FOR           : 'for';
IF            : 'if';
GOTO          : 'goto';
IMPLEMENTS    : 'implements';
IMPORT        : 'import';
INSTANCEOF    : 'instanceof';


#INT           : 'int';

INTERFACE     : 'interface';


#LONG          : 'long';

NATIVE        : 'native';
NEW           : 'new';
NON_SEALED    : 'non-sealed';

PACKAGE       : 'package';
PERMITS       : 'permits';
PRIVATE       : 'private';
PROTECTED     : 'protected';
PUBLIC        : 'public';

RECORD        : 'record';
RETURN        : 'return';

SEALED        : 'sealed';


#SHORT         : 'short';


STATIC        : 'static';
STRICTFP      : 'strictfp';
SUPER         : 'super';
SWITCH        : 'switch';
SYNCHRONIZED  : 'synchronized';
THIS          : 'this';
THROW         : 'throw';
THROWS        : 'throws';
TRANSIENT     : 'transient';
TRY           : 'try';
VOID          : 'void';
VOLATILE      : 'volatile';
WHILE         : 'while';

ELLIPSIS: "...";
 AT: "@";

LPAREN          : '('  ;
RPAREN          : ')'  ;

LBRACE          : '{'  ;
RBRACE          : '}'  ;

LBRACK          : '['  ;
RBRACK          : ']'  ;

SEMI            : ';';
COMMA           : ',';
DOT             : ".";

 // §3.12 Operators

 ASSIGN          : '=';
 GT              : '>';
 LT              : '<';
 NOT             : '!';
 BITNOT          : '~';
 QUESTION        : '?';
 COLON           : ':';
 EQUAL           : '==';
 LE              : '<=';
 GE              : '>=';
 NOTEQUAL        : '!=';
 AND             : '&&';
 OR              : '||';
 INC             : '++';
 DEC             : '--';
 ADD             : '+';
 SUB             : '-';
 MUL             : '*';
 DIV             : "/";
 BITAND          : '&';
 BITOR           : '|';
 XOR             : '^';
 MOD             : '%';


 ADD_ASSIGN      : '+=';
 SUB_ASSIGN      : '-=';
 MUL_ASSIGN      : '*=';
 DIV_ASSIGN      : '/=';
 AND_ASSIGN      : '&=';
 OR_ASSIGN       : '|=';
 XOR_ASSIGN      : '^=';
 MOD_ASSIGN      : '%=';
 LSHIFT_ASSIGN   : '<<=';
 RSHIFT_ASSIGN   : '>>=';
 URSHIFT_ASSIGN  : '>>>=';
 ELVIS_ASSIGN    : '?=';

 ARROW: "->";


 WS  : ([ \t]+ | ("\\" LineTerminator)+) -> skip;
 #LineTerminator: '\r'? '\n' | '\r';
 NL: LineTerminator   /*{ this->ignoreTokenInsideParens(); }*/;
 ML_COMMENT:   [:block_comment:] -> skip;
 SL_COMMENT:   '//' [^\r\n\uFFFF]* -> skip;

 GString: '"' (('\\"' | [^$"])* "$" [a-z]+)* '"';
 StringLiteral: [:char:] | '"' ('\\"' |[^"])* '"';
 IntegerLiteral: [0-9]+;
 FloatingPointLiteral: [0-9]+ "." [0-9]+;
 BooleanLiteral: "true" | "false";
 NullLiteral: "null";

 CapitalizedIdentifier:   [A-Z] JavaLetterOrDigit*;
 Identifier: JavaLetter JavaLetterOrDigit*;
 #JavaLetter: [a-zA-Z$_] | [^\u0000-\u007F\uD800-\uDBFF] | [\uD800-\uDBFF] [\uDC00-\uDFFF];
 #JavaLetterOrDigit: JavaLetter | [0-9];
}