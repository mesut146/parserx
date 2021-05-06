include "javaLexer.g"

token{
 REST: "hello";
 R2: "r";
}

/*E:
   E "++"
 | E "*" E
 | E "+" E
 | REST;
*/

E: A "a";
A: B "b" | C "c";
B: A | E "e" | "r";
C: B "b" | "r";



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