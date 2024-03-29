token{
 NUM: [0-9]+;
 LP: "(";
 RP: ")";
 EQ: "=";
 ARROW: "->";
 COMMA: ",";
 BRACKET: "[" ([^\r\n\\\u005d] | "\\" .)* "]";
 START: "start" | "initial";
 FINAL: "final" | "acc";
 IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
 nls: [\r\n]+;
 ANY: [^ \t];
}

token{
  ws: [ \t]+ -> skip;
  comment: [:line_comment:] -> skip;
}

nfa: nls? startDecl nls finalDecl nls trLine+;
trLine: (trArrow | trSimple) nls?;
startDecl: START "=" NUM;
finalDecl: FINAL "=" finalList;
finalList: namedState ("," namedState)*;
namedState: NUM ("(" IDENT ")")?;
trArrow: NUM "->" NUM ("," INPUT)?;
trSimple: NUM NUM INPUT?;
INPUT: BRACKET | IDENT | ANY | NUM;