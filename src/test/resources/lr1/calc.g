include "calc-tokens.g"

%start = E;

E: "-" E | "(" E ")" | NUM
 | E "^" E %left
 | E ("*" | "/") E %left
 | E ("+" | "-") E %left;


