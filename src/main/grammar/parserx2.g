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

skip{
  LINE_COMMENT: "//" [^\n]*;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/";
  WS: [ \n\r\t]+;
}

%start: tree;

tree: includeStatement* optionsBlock? blocks=modeBlock* startDecl? rules=ruleDecl*;

includeStatement: "include" STRING;

optionsBlock: "options" "{" option* "}";
option: key=IDENT "=" value=(NUMBER | BOOLEAN) ";"?;

startDecl: START SEPARATOR name ";";


tokenDecl: "#"? name SEPARATOR rhs modeSpec? ";";
modeSpec: "->" name ("," name)*;
modeBlock: name "{" modeMember "}";
modeMember: tokenDecl | modeBlock;

ruleDecl: name args? SEPARATOR rhs ";";
args: "(" name rest=("," name)* ")";
//args: "(" %join(name, ",")  ")";


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
     | repeatNode
     | SHORTCUT
     | call;

group: "(" rhs ")";
stringNode: STRING | CHAR;
bracketNode: BRACKET;//easier to handle as token
untilNode: "~" regex;
dotNode: ".";
name: IDENT | TOKEN | "skip" | "options" | "include";
repeatNode: "{" rhs "}";

call: CALL_BEGIN IDENT ("," IDENT)* ")";

join: "%join" "(" nameOrString: "," nameOrString: ")";
nameOrString: name | stringNode;

//bracketOpt: "[" rhs "]";
