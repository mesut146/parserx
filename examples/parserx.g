token{
 BOOLEAN: "true" | "false";
 OPTION: "option";
 TOKEN: "token";
 SKIP: "skip";
 START: "@start";
 EPSILON: "%epsilon" | "ε";
 IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
}

token{
 LP: "(";
 RP: ")";
 LBRACE: "{";
 RBRACE: "}";
 STAR: "*";
 PLUS: "+";
 QUES: "?";
 POW: "^";
 SEPARATOR: ":" | "=" | ":=" | "::=" | "->";
}

skip{
  LINE_COMMENT: "//" [^\n]*;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/";
  WS: [ \n\r\t]+;

}

optionsBlock: "options" "{" option* "}";
opiton: IDENT "=" (NUMBER | BOOLEAN);


tree: includeStatement* (tokenBlock  | skipBlock)* startDecl? ruleDecl*;

includeStatement: "include" <STRING_LITERAL>;
startDecl: "@start" "=" name;

tokenBlock: "token" "{" tokenDecl* "}";
skipBlock: "skip" "{" tokenDecl* "}";


tokenDecl= "#"? name SEPARATOR rhs;
ruleDecl= name args? SEPARATOR rhs;
args: "(" name ("," name)* ")";

rhs: sequence ("|" sequence)*;
sequence: regex+;
regex: simple ("*" | "+" | "?")?
simple: group | ref | stringNode | bracketNode | untilNode | dotNode | EPSILON;

group: "(" rhs ")";
stringNode: <STRING_LITERAL>
bracketNode: <BRACKET_LIST>//easier to handle as token
untilNode: "~" regex;
dotNode: "."
ref: lexerRef | name;
lexerRef: "{" name "}";
name: IDENT;