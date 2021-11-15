token{
 NUM: [0-9]+;
 POW: "^";
 PLUS: "+";
 MINUS: "-";
 MUL: "*";
 DIV: "/";
 LP: "(";
 RP: ")";
}
//E: E "*" E | "-" E | E "+" E | NUM;


//%left PLUS MINUS MUL DIV;

E:
   "-" E
 | E "^" E
 | E ("*" | "/") E
 | E ("+" | "-") E
 | "(" E ")";
 | NUM;

/*
E: POW0 "^" POW0 | MUL0 "*" MUL0 | E "+" E | PRIM;
E: MUL0 | E "+" E;
MUL0: MUL0 "*" MUL0 | POW0;
POW0: POW0 "^" POW0 | PRIM;

//right assoc
E: MUL0 ("+" E)*;
//left assoc
E: MUL0 ("+" MUL0)*;

//right assoc
MUL0: POW0 ("*" MUL0)*;
//left assoc
MUL0: POW0 ("*" POW0)*;

//right assoc
POW0: PRIM ("^" POW0)*;
//left assoc
POW0: PRIM ("^" PRIM)*;

PRIM: "-" PRIM | (" E ")" | NUM;
*/


/*E: REST
| ("+" | "-" | "++" | "--" | "!" | "~") E #unary
| E ("++" | "--") #post
| E ("*" | "/" | "%") E
| E ("+" | "-") E
| E ("<<" | ">>" | ">>>") E
| E ("<" | "<=" | ">" | ">=") E
| E ("==" | "!=") E
| E "&" E
| E "^" E
| E "|" E
| E "&&" E
| E "||" E;*/