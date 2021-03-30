token{
  a: "";
  LP: "(";
  RP: ")";
  PLUS: "+";
}

S -> F;
S -> "(" S "+" F ")";
F -> a;