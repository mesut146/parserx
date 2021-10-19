include "../common.g"

%start = B;

A: B b | c;
B: A a | d;

/*
A: (A a | d) b | c;
B: (B b | c) a | d;
B: (c a | d) b*;

B B(){
  B res = B_no_B();
  while(la == b){
    res = B(res);
  }
  return res;
}
B B(A Af1){
 B res;
 return res;
}

B B_no_B(){
  B res = new B();
  if(la == c){
    res.which = 1;
    B1 b1 = res.b1 = new B1();
    A res2 = new A();
    res2.which = 2;
    res2.c = consume(c);
    res.A = res2;
    res.a = consume(a);
  }else if(la == d){
    res.which = 2;
    res.d = consume(d);
  }
  return res;
}


*/

/*
A: A a b | d b | c;
A: (d b | c) (a b)*;
B: (d b | c) (a b)* a | d;

B: B b | c a | d;
B: (c a | d) b*;
A: (c a | d) b* b | c;
*/

/*
A: A1 A2(A)*;
A1: B1 b | c;
A2: B2 b;
A3: A5 A2(A)*;
A4: A6 A2(A)*;
A5: c;
A6: B4 b;
B: d (A4 a | d(d)) | A3 a;
B1: d;
B2: A(A) a;
B4: d(d);
*/

