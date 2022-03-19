token{
 BOOLEAN: 'true' | 'false';
 OPTIONS: "options";
 TOKEN: "token" | "tokens";
 SKIP: "skip";
 INCLUDE: "include";
 START: "%start";
 EPSILON: "%epsilon" | "%empty" | "ε";
 LEFT: "%left";
 RIGHT: "%right";
 JOIN: "%join";
 IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
 CALL_BEGIN: IDENT "(";
 SHORTCUT: "[:" IDENT ":]";
 BRACKET: "[" ([^\r\n\\\u005d] | "\\" .)* "]";
 STRING: "\"" ([^\r\n\\"] | "\\" .)* "\"";
 CHAR: "'" ([^\r\n\\'] | "\\" .)* "'";
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
 MINUS: "-";
}

skip{
  LINE_COMMENT: "//" [^\n]*;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/";
  WS: [ \n\r\t]+;
}

%start: tree;

tree: includeStatement* optionsBlock? tokens=(tokenBlock  | skipBlock)* startDecl? rules=ruleDecl*;

includeStatement: "include" STRING;

optionsBlock: "options" "{" option* "}";
option: key=IDENT "=" value=(NUMBER | BOOLEAN) ";"?;

startDecl: START SEPARATOR name ";";

tokenBlock: TOKEN "{" tokenDecl* "}";
skipBlock: "skip" "{" tokenDecl* "}";


tokenDecl: "#"? name ("-" name)? SEPARATOR rhs ";";
ruleDecl: name args? SEPARATOR rhs ";";
args: "(" name rest=("," name)* ")";
//args: "(" %join(name, ",")  ")";

rhs: sequence ("|" sequence)*;
sequence: regex+ assoc=("%left" | "%right")? label=("#" name)?;

regex: name "=" simple type=("*" | "+" | "?")?
     | simple type=("*" | "+" | "?")?;

simple: group | name | stringNode | bracketNode | untilNode | dotNode | EPSILON | repeatNode | SHORTCUT | call;

group: "(" rhs ")";
stringNode: STRING | CHAR;
bracketNode: BRACKET;//easier to handle as token
untilNode: "~" regex;
dotNode: ".";
name: IDENT | "token" | "tokens" | "skip" | "options" | "include";
repeatNode: "{" rhs "}";

call: CALL_BEGIN IDENT ("," IDENT)* ")";

join: "%join" "(" nameOrString: "," nameOrString: ")";
nameOrString: name | stringNode;

//bracketOpt: "[" rhs "]";

