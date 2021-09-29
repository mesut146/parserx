token{
  a = "a";
  b = "b";
  c = "c";
}

%start = E;
E = A b;
A = A a;
A = c;