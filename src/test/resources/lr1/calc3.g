include "calc-tokens.g"

%start = E;
E: NUM | NUM "+" E | "(" E ")";

//E: (NUM "+")* (NUM | "(" E ")");


