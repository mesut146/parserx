token{
  NUM: [0-9]+;
  LP: "(";
  RP: ")";
  EQ: "=";
  ARROW: "->";
  COMMA: ",";
  BRACKET: "[" ([^\r\n\\\]] | "\\" .)* "]";
  START: "start" | "initial";
  FINAL: "final" | "acc";
  IDENT: [a-zA-Z_] [a-zA-Z0-9_]*;
  nls: [\r\n]+;
  ANY: [^\u0020\t];
}

skip{
  ws: [\u0020\t]+;
  comment: ("/" "/" [^\n]*);
}

/* rules */
nfa: startDecl nls finalDecl nls trLine+;
trLine: trLineg1 nls?;
trLineg1: NUM (trArrow_NUM(NUM) | trSimple_NUM(NUM));
startDecl: START EQ NUM;
finalDecl: FINAL EQ finalList;
finalList: namedState (namedState() namedState* | namedState() finalListg1*);
finalListg1: COMMA namedState;
namedState: NUM namedStateg1?;
namedStateg1: LP IDENT RP;
trArrow: NUM ARROW NUM trArrowg1?;
trArrow_NUM(NUM): NUM() ARROW NUM trArrowg1?;
trArrowg1: COMMA INPUT;
trSimple: NUM NUM INPUT?;
trSimple_NUM(NUM): NUM() NUM INPUT?;
INPUT:
    BRACKET
|   IDENT
|   ANY
;