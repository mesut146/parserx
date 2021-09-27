token{
  a = "a";
  b = "b";
  c = "c";
}

%start = expr;
expr = A b;
A = A a;
A = c;