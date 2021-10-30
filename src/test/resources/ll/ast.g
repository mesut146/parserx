token{
  PLUS: "+";
  STAR: "*";
  NUM: [0-9]+;
}

E: TERM "+" E | TERM;
TERM: NUM "*" NUM | NUM;