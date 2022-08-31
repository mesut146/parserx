include "calc-tokens.g"

%start: E;

E: "(" E ")" | NUM
 | E "^" E %left
 | "-" E
 | E ("*" | "/") E %left
 | E ("+" | "-") E %left;

