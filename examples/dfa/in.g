token{
  //NUM: [0-9]+;
  //PLUS: "+";
  //MINUS: "-";
  //MUL: "*";
  //DIV: "/";
  str: "\"" ("\\" . | [^\r\n\\"])* "\"";
}