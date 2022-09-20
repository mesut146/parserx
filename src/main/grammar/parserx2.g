token{
 BOOLEAN: "true" | "false";
 OPTIONS: "options";
 TOKEN: "token";
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
 STAR: "*";
 PLUS: "+";
 QUES: "?";
 POW: "^";
 SEPARATOR: ":" | "=" | ":=" | "::=";
 TILDE: "~";
 HASH: "#";
 COMMA: ",";
 OR: "|";
 DOT: ".";
 SEMI: ";";
 MINUS: "-";
 ARROW: "->";
}

token{
  LINE_COMMENT: "//" [^\n]* -> skip;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/" -> skip;
  WS: [ \n\r\t]+ -> skip;
}

%start: tree;

tree: includeStatement* optionsBlock? tokens=tokenBlock* startDecl? rules=ruleDecl*;

includeStatement: "include" STRING;

optionsBlock: "options" "{" option* "}";
option: key=IDENT "=" value=(NUMBER | BOOLEAN) ";"?;

startDecl: START SEPARATOR name ";";

tokenBlock: "token" "{" (tokenDecl | modeBlock)* "}";
tokenDecl: "#"? name SEPARATOR rhs mode=("->" modes)? ";";
modes: name ("," name)?;
modeBlock: name "{" tokenDecl* "}";

ruleDecl: name args? SEPARATOR rhs ";";
args: "(" name rest=("," name)* ")";

rhs: sequence ("|" sequence)*;
sequence: regex+ assoc=("%left" | "%right")? label=("#" name)?;

regex: name "=" simple type=("*" | "+" | "?")?
     | simple type=("*" | "+" | "?")?;

simple: group
     | name
     | stringNode
     | bracketNode
     | untilNode
     | dotNode
     | EPSILON
     | SHORTCUT
     | call;

group: "(" rhs ")";
stringNode: STRING | CHAR;
bracketNode: BRACKET;//easier to handle as token
untilNode: "~" regex;
dotNode: ".";
name: IDENT | TOKEN | "options" /*| "include"*/;

call: CALL_BEGIN IDENT ("," IDENT)* ")";

join: "%join" "(" nameOrString: "," nameOrString: ")";
nameOrString: name | stringNode;

//bracketOpt: "[" rhs "]";

