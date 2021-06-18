token
{
  plus: "+";
  lp: "(";
  rp: ")";
  N: [0-9]+;
}

//productions

@start = E;
E: N;
E: N "+" E;
E: "(" E ")";


