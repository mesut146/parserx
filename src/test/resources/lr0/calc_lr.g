token{
  PLUS = "+";
  STAR = "*";
  NUMBER = ["1"-"9"] ["0-"9"]*;
}

@start = E;

E:  E "+" E | E1 | NUMBER;
E1: E1 "*" E1 | NUMBER;