include "../common.g"

A: a (B b | a c);
B: a d | e;


/*
A: a Ag1;
Ag1: B b | a c;

Ag1: a (B(a) b | a(a) c)  | B_no_a b;
B(a): a(a) d;
B_no_a: e;

B B(Token a){
 B res;
 res.which = 1;
 res.a = a;
 res.d = consume(d);
 return res;
}

B B_no_a(){
 B res;
 res.which = 2;
 res.e = consume(e);
 return res;
}

*/