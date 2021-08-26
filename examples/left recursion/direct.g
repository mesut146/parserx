token{
  PLUS = "+";
  NUM = [0-9]+;
}

/* rules */
E = E "+" E | NUM;