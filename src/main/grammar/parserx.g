token{
 BOOLEAN: "true" | "false";
 OPTIONS: "options";
 TOKEN: "token";
 INCLUDE: "include";
 START: "%start";
 EPSILON: "%epsilon" | "%empty" | "ε";
 LEFT: "%left";
 RIGHT: "%right";
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
 //POW: "^";
 SEPARATOR: ":";
 TILDE: "~";
 HASH: "#";
 COMMA: ",";
 OR: "|";
 DOT: ".";
 SEMI: ";";
 ARROW: "->";
 EQ: "=";
 MINUS: "-";
}

token{
  LINE_COMMENT: "//" [^\r\n]* -> skip;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/" -> skip;
  WS: [ \n\r\t]+ -> skip;
}

token{
  ACTION: "@{" ([^}] | "}"+ [^@])* "}@";
  LEXER_MEMBERS_BEGIN: "lexerMembers" WS? "{" -> member_mode;
  member_mode{
    WS1: WS -> skip;
    LEXER_MEMBER: [^\n;}]+ ";";
    MEMBERS_END: "}" -> DEFAULT;
  }
}

%start: tree;

tree: includeStatement* optionsBlock? lexerMembers? tokens=tokenBlock* startDecl? rules=ruleDecl*;

lexerMembers: LEXER_MEMBERS_BEGIN LEXER_MEMBER+ MEMBERS_END;

includeStatement: "include" STRING;

optionsBlock: "options" "{" option* "}";
option: key=IDENT "=" value=(NUMBER | BOOLEAN) ";"?;

startDecl: START SEPARATOR name ";";

tokenBlock: "token" "{" (tokenDecl | modeBlock)* "}";
tokenDecl: "#"? name SEPARATOR rhs mode=("->" modes)? ";";
modes: name ("," name)?;
modeBlock: IDENT "{" tokenDecl* "}";

ruleDecl: name args? SEPARATOR rhs ";";
args: "(" param rest=("," param)* ")";
param: name name;

rhs: sequence ("|" sequence)*;
sequence: sub+ assoc=("%left" | "%right")? label=("#" name)?;

sub: regex ("-" stringNode)?;

regex: name "=" simple type=regexType? ACTION?
     | simple type=regexType? ACTION?;
regexType: "*" | "+" | "?";

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
//easier to handle as token
bracketNode: BRACKET;
untilNode: "~" regex;
dotNode: ".";
name: IDENT;

call: CALL_BEGIN IDENT ("," IDENT)* ")";
