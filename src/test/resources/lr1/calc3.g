token
{
  a = "a";
  b = "b";
  c = "c";
  d = "d";
}

//productions

@start = S;
S: A "c";
A: "a" "b";
A: "a";


