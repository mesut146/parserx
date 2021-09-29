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
 BRACKET: "[" ~"]";
 SHORTCUT: "[:" IDENT ":]";
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


tree: includeStatement* optionsBlock? tokens=(tokenBlock  | skipBlock)* startDecl? rules=(ruleDecl | assocDecl)*;

includeStatement: "include" STRING;

optionsBlock: "options" "{" option* "}";
option: key=IDENT "=" value=(NUMBER | BOOLEAN) ";"?;

startDecl: START SEPARATOR name ";";

tokenBlock: TOKEN "{" tokenDecl* "}";
skipBlock: "skip" "{" tokenDecl* "}";


tokenDecl: "#"? name SEPARATOR rhs ";";
ruleDecl: name args? SEPARATOR rhs ";";
args: "(" name rest=("," name)* ")";

assocDecl: type=("%left" | "%right") ref+ ";";

rhs: sequence ("|" sequence)*;
sequence: regex+ label=("#" name)?;
regex: name=(name "=")? simple type=("*" | "+" | "?")?;
simple: group | ref | stringNode | bracketNode | untilNode | dotNode | EPSILON | repeatNode | SHORTCUT;

group: "(" rhs ")";
stringNode: STRING;
bracketNode: BRACKET;//easier to handle as token
untilNode: "~" regex;
dotNode: ".";
ref: name;
name: IDENT | "token" | "tokens" | "skip" | "options";
repeatNode: "{" rhs "}";

//bracketOpt: "[" rhs "]";

