token{
  PLUS = "+";
  STAR = "*";
  NUMBER = ["1"-"9"] ["0-"9"]*;
}

@start = expr;

expr = mul expr1;
expr1 = | expr1 "+" mul;
mul = term mul1;
mul1 = | mul1 "*" term;
term = NUMBER;