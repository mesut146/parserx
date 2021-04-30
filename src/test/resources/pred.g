token{
 PLUS: "+";
 MINUS: "-";
 MUL: "*";
 DIV: "/";
 POW: "^";
 AND: "&";
 s: "hello";
}

E: E ("+" | "-") E;
E: E ("*" | "/") E;
E: E ("^") E;
E: E "&" E;
E: "hello";
