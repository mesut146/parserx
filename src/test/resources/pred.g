include "javaLexer.g"

token{
 REST: "hello";
}

/*E: E "^" E
 | E "*" E
 | E "+" E
 | REST;
*/

E: REST
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
| E "||" E;