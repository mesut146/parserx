include "GroovyLexer.g"

nls: NL*;

sep: (NL | SEMI)+;

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

Unit: nls scriptStatements?;

//scriptStatements: scriptStatement (sep scriptStatement)* sep?;
scriptStatements: scriptStatement scriptStatements0?;
scriptStatements0: sep (scriptStatement scriptStatements0?)?;


scriptStatement: statement;

block: LBRACE sep? blockStatements? RBRACE;

blockStatements: blockStatement tmp?;
//tmp: (sep blockStatement)* sep?;
tmp: sep (blockStatement tmp?)?;

blockStatement: /*localVariableDeclaration |*/ statement;

statement: expression | methodCall_no_lp | SEMI;

expression: methodCall | assign | literal | list | closure;

assign: qname (op rhs)?;
op: '=';
//op: "=" | "+=" | "-=" | "*=" | "/=" | "&=" | "|=" | "^=" | "%=" | "<<=" | ">>=" | ">>>=" | "?=";
rhs: expression | methodCall_no_lp;

qname: identifier ("." identifier)*;

methodCall_no_lp: identifier arguments;
methodCall: identifier "(" arguments? ")" closure?;
arguments: nls argument ("," nls argument)*;
argument: expression | namedArg;

namedArg: identifier ":" expression;
closure: "{" /*(nls (formalParameterList nls)? ARROW)?*/ sep? blockStatements? "}";

formalParameterList: %empty;

literal: StringLiteral | IntegerLiteral | FloatingPointLiteral | BooleanLiteral | NullLiteral | GString;

list: "[" exprList? "]";

exprList: expression ("," expression)*;