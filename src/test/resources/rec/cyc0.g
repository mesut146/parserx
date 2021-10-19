include "../common.g"

A: B b | c;
B: A a;

/*
B: {B b | c} a;
B: B1 B2(B)*;
B1: c a;
B2(B): B(B) b a;

B B_no_B(){
  B res = new B();
  res.A = A_no_B();
  return res;
}

B B(){
  B res = B_no_B();
  while(la == b){
    res = B(res);
  }
  return res;
}

*/