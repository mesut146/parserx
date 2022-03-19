token{
  BOOLEAN: "true" | "false";
  OPTIONS: "options";
  TOKEN: "token" | "tokens";
  SKIP: "skip";
  INCLUDE: "include";
  START: "%start";
  EPSILON: "%epsilon" | "%empty" | "??";
  LEFT: "%left";
  RIGHT: "%right";
  JOIN: "%join";
  IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
  CALL_BEGIN: [a-zA-Z_] [a-zA-Z0-9_]* "(";
  SHORTCUT: "[:" [a-zA-Z_] [a-zA-Z0-9_]* ":]";
  BRACKET: "[" ([^\r\n\\\]] | "\\" .)* "]";
  STRING: "\"" ([^\r\n\\"] | "\\" .)* "\"";
  CHAR: "'" ([^\r\n\\'] | "\\" .)* "'";
  NUMBER: [0-9]+;
  LP: "(";
  RP: ")";
  LBRACE: "{";
  RBRACE: "}";
  STAR: "*";
  PLUS: "+";
  QUES: "?";
  POW: "^";
  SEPARATOR: ":" | "=" | ":=" | "::=" | "->";
  TILDE: "~";
  HASH: "#";
  COMMA: ",";
  OR: "|";
  DOT: ".";
  SEMI: ";";
  MINUS: "-";
}

skip{
  LINE_COMMENT: "//" [^\n]*;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/";
  WS: [\u0020\n\r\t]+;
}

/* rules */
%start = tree;

tree: includeStatement* optionsBlock? treeg1* startDecl? ruleDecl*;
treeg1:
    tokenBlock
|   skipBlock
;
includeStatement: INCLUDE STRING;
optionsBlock: OPTIONS LBRACE option* RBRACE;
option: IDENT SEPARATOR optiong1 SEMI?;
optiong1:
    NUMBER
|   BOOLEAN
;
startDecl: START SEPARATOR name SEMI;
tokenBlock: TOKEN LBRACE tokenDecl* RBRACE;
skipBlock: SKIP LBRACE tokenDecl* RBRACE;
tokenDecl: HASH? name tokenDeclg1? SEPARATOR rhs SEMI;
tokenDeclg1: MINUS name;
ruleDecl: name args? SEPARATOR rhs SEMI;
args: LP name argsg1* RP;
argsg1: COMMA name;
rhs: sequence rhsg1*;
rhsg1: OR sequence;
sequence: regex+ sequenceg1? sequenceg2?;
sequenceg2: HASH name;
sequenceg1:
    LEFT
|   RIGHT
;
regex:
    (name (name() SEPARATOR simple regexg1? | simple_name(name) regexg2?))
|   (simple_no_name regexg2?)
;
regexg2:
    STAR
|   PLUS
|   QUES
;
regexg1:
    STAR
|   PLUS
|   QUES
;
simple:
    group
|   name
|   stringNode
|   bracketNode
|   untilNode
|   dotNode
|   EPSILON
|   repeatNode
|   SHORTCUT
|   call
;
simple_no_name:
    group
|   stringNode
|   bracketNode
|   untilNode
|   dotNode
|   EPSILON
|   repeatNode
|   SHORTCUT
|   call
;
simple_name(name): name();
group: LP rhs RP;
stringNode:
    STRING
|   CHAR
;
bracketNode: BRACKET;
untilNode: TILDE regex;
dotNode: DOT;
name:
    IDENT
|   TOKEN
|   SKIP
|   OPTIONS
|   INCLUDE
;
repeatNode: LBRACE rhs RBRACE;
call: CALL_BEGIN IDENT callg1* RP;
callg1: COMMA IDENT;
join: JOIN LP COMMA RP;
nameOrString:
    name
|   stringNode
;