include "calc-tokens.g"

//productions

%start = E;
E: NUM | NUM "+" E | "(" E ")";

//E: (NUM "+")* (NUM | "(" E ")");


