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
  LINE_COMMENT: "//" [^\n]* -> skip;
  BLOCK_COMMENT: "/*" ([^*] | "*" [^/])* "*/" -> skip;
  WS: [ \n\r\t]+ -> skip;
}

token{
  ACTION_REF: "@" IDENT;
  ACTION_TOKEN: "action";
  //ACTION: "%begin" ([^%] | "%" [^e] | "%e" [^n] | "%en" [^d])+ "%end";
  ACTION: "@{" ([^}] | "}" [^@])* "}@";
  LEXER_MEMBERS_BEGIN: "lexerMembers" WS? "{" -> member_mode;
  member_mode{
    WS1: WS -> skip;
    LEXER_MEMBER: [^\n;}]+ ";";
    MEMBERS_END: "}" -> DEFAULT;
  }
}

action{

}

%start: tree;

tree: includeStatement* optionsBlock? lexerMembers? tokens=tokenBlock* actionBlock? startDecl? rules=ruleDecl*;

lexerMembers: LEXER_MEMBERS_BEGIN LEXER_MEMBER+ MEMBERS_END;

actionBlock: "action" "{" actionEntry* "}";
actionEntry: IDENT ":" ACTION;

includeStatement: "include" STRING;

optionsBlock: "options" "{" option* "}";
option: key=IDENT "=" value=(NUMBER | BOOLEAN) ";"?;

startDecl: START SEPARATOR name ";";

tokenBlock: "token" "{" (tokenDecl | modeBlock)* "}";
tokenDecl: "#"? name SEPARATOR rhs mode=("->" modes)? ";";
modes: name ("," name)?;
modeBlock: IDENT "{" tokenDecl* "}";

ruleDecl: name args? SEPARATOR rhs ";";
args: "(" name rest=("," name)* ")";

rhs: sequence ("|" sequence)*;
sequence: sub+ assoc=("%left" | "%right")? label=("#" name)?;

sub: regex ("-" stringNode)?;

regex: name "=" simple type=regexType? ACTION_REF?
     | simple type=regexType? ACTION_REF?;
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
bracketNode: BRACKET;//easier to handle as token
untilNode: "~" regex;
dotNode: ".";
name: IDENT;

call: CALL_BEGIN IDENT ("," IDENT)* ")";
