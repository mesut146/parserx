token
{
  plus: "+";
  star: "*";
  lp: "(";
  rp: ")";
  id: "id";
}

//productions

@start = E;
E: E "+" T;
E: T;
T: T "*" F;
T: F;
F: "(" E ")";
F: id;


