token{
 BOOLEAN: "true" | "false";
 OPTIONS: "options";
 TOKEN: "token" | "tokens";
 SKIP: "skip";
 INCLUDE: "include";
 START: "%start";
 EPSILON: "%epsilon" | "ε";
 LEFT: "%left";
 RIGHT: "%right";
 IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
 SHORTCUT: "[:" IDENT ":]";
 BRACKET: "[" ~"]";
 STRING: "\"" ([^\r\n\\"] | "\\" .)* "\"";
 NUMBER: [0-9]+;
}

/*token{
 #bracketLexer: "[" "^"? unit+ "]";
 #unit: single ("-" single)?;
 #single: "\\u" hex_digit hex_digit hex_digit hex_digit | .;
 #hex_digit: [a-fA-F0-9];
}*/

token{
 LP: "(";
 RP: ")";
 LBRACE: "{";
 RBRACE: "}";
 //LBRACKET: "[";
 //RBRACKET: "]";
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
}

skip{
  LINE_COMMENT: "//" [^\n]*;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/";
  WS: [ \n\r\t]+;
}

%start: tree;

tree:
    rules = (ruleDecl | assocDecl)+
|   startDecl
|   startDecl rules = (ruleDecl | assocDecl)+
|   tokens = (tokenBlock | skipBlock)+
|   tokens = (tokenBlock | skipBlock)+ rules = (ruleDecl | assocDecl)+
|   tokens = (tokenBlock | skipBlock)+ startDecl
|   tokens = (tokenBlock | skipBlock)+ startDecl rules = (ruleDecl | assocDecl)+
|   optionsBlock
|   optionsBlock rules = (ruleDecl | assocDecl)+
|   optionsBlock startDecl
|   optionsBlock startDecl rules = (ruleDecl | assocDecl)+
|   optionsBlock tokens = (tokenBlock | skipBlock)+
|   optionsBlock tokens = (tokenBlock | skipBlock)+ rules = (ruleDecl | assocDecl)+
|   optionsBlock tokens = (tokenBlock | skipBlock)+ startDecl
|   optionsBlock tokens = (tokenBlock | skipBlock)+ startDecl rules = (ruleDecl | assocDecl)+
|   includeStatement+
|   includeStatement+ rules = (ruleDecl | assocDecl)+
|   includeStatement+ startDecl
|   includeStatement+ startDecl rules = (ruleDecl | assocDecl)+
|   includeStatement+ tokens = (tokenBlock | skipBlock)+
|   includeStatement+ tokens = (tokenBlock | skipBlock)+ rules = (ruleDecl | assocDecl)+
|   includeStatement+ tokens = (tokenBlock | skipBlock)+ startDecl
|   includeStatement+ tokens = (tokenBlock | skipBlock)+ startDecl rules = (ruleDecl | assocDecl)+
|   includeStatement+ optionsBlock
|   includeStatement+ optionsBlock rules = (ruleDecl | assocDecl)+
|   includeStatement+ optionsBlock startDecl
|   includeStatement+ optionsBlock startDecl rules = (ruleDecl | assocDecl)+
|   includeStatement+ optionsBlock tokens = (tokenBlock | skipBlock)+
|   includeStatement+ optionsBlock tokens = (tokenBlock | skipBlock)+ rules = (ruleDecl | assocDecl)+
|   includeStatement+ optionsBlock tokens = (tokenBlock | skipBlock)+ startDecl
|   includeStatement+ optionsBlock tokens = (tokenBlock | skipBlock)+ startDecl rules = (ruleDecl | assocDecl)+
;
includeStatement = INCLUDE STRING;
optionsBlock:
    OPTIONS LBRACE RBRACE
|   OPTIONS LBRACE option+ RBRACE
;
option:
    key = IDENT SEPARATOR value = (NUMBER | BOOLEAN)
|   key = IDENT SEPARATOR value = (NUMBER | BOOLEAN) SEMI
;
startDecl = START SEPARATOR name SEMI;
tokenBlock:
    TOKEN LBRACE RBRACE
|   TOKEN LBRACE tokenDecl+ RBRACE
;
skipBlock:
    SKIP LBRACE RBRACE
|   SKIP LBRACE tokenDecl+ RBRACE
;
tokenDecl:
    name SEPARATOR rhs SEMI
|   HASH name SEPARATOR rhs SEMI
;
ruleDecl = ref SEPARATOR rhs SEMI;
assocDecl = type = (LEFT | RIGHT) ref+ SEMI;
rhs:
    sequence
|   sequence (OR sequence)+
;
sequence:
    regex+
|   regex+ label = (HASH name)
;
regex:
    simple
|   simple type = (STAR | PLUS | QUES)
|   name = (name SEPARATOR) simple
|   name = (name SEPARATOR) simple type = (STAR | PLUS | QUES)
;
simple:
    group
|   ref
|   stringNode
|   bracketNode
|   untilNode
|   dotNode
|   EPSILON
|   repeatNode
|   SHORTCUT
;
group = LP rhs RP;
stringNode = STRING;
bracketNode = BRACKET;
untilNode = TILDE regex;
dotNode = DOT;
ref:
    name
|   name args
;
args:
    LP ref RP
|   LP ref rest = (COMMA ref)+ RP
;
arg:
    name
|   name type = (STAR | PLUS)
;
name:
    IDENT
|   TOKEN
|   SKIP
|   OPTIONS
;
repeatNode = LBRACE rhs RBRACE;


