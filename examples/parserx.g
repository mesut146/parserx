token{
 BOOLEAN: "true" | "false";
 OPTIONS: "options";
 TOKEN: "token" | "tokens";
 SKIP: "skip";
 INCLUDE: "include";
 START: "@start";
 EPSILON: "%epsilon" | "ε";
 IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
 BRACKET: "[" ~"]";
 STRING: "\"" ([^\r\n\\"] | "\\" .)* "\"";
 NUMBER: [0-9]+;
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

optionsBlock: "options" "{" option* "}";
option: IDENT "=" (NUMBER | BOOLEAN);


tree: includeStatement* (tokenBlock  | skipBlock)* startDecl? ruleDecl*;

includeStatement: "include" STRING;
startDecl: "@start" SEPARATOR name ";";

tokenBlock: ("token" | "tokens") "{" tokenDecl* "}";
skipBlock: "skip" "{" tokenDecl* "}";


tokenDecl= "#"? name SEPARATOR rhs ";";
ruleDecl= name args? SEPARATOR rhs ";";
args: "(" name ("," name)* ")";

rhs: sequence ("|" sequence)*;
sequence: regex+ label=("#" name)?;
regex: (name "=")? simple ("*" | "+" | "?")?;
simple: group | ref | stringNode | bracketNode | untilNode | dotNode | EPSILON | repeatNode;

group: "(" rhs ")";
stringNode: STRING;
bracketNode: BRACKET;//easier to handle as token
untilNode: "~" regex;
dotNode: ".";
ref: name;
name: IDENT;
repeatNode: "{" rhs "}";

test: "+" #lbl1
    | "*" #lbl2;

