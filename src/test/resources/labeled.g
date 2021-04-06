token{
  PLUS: "+";
  MUL: "*";
}

E: left = E op = "+" right = E #add
 | left = E op = "*" right = E #mul
;