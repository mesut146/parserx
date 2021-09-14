token
{
  plus: "+";
  lp: "(";
  rp: ")";
  N: [0-9]+;
}

//productions

@start = E;
E: N | N "+" E | "(" E ")";

//E: (N "+")* (N | "(" E ")");


