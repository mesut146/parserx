token{
  OPT: "a" "b"?;
  STAR: "c" "d"*;
  PLUS: "e"+;
}

token{
 ws: [ ] -> skip;
}
