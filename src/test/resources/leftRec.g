token{
 x : "";
 y : "";
 a : "";
 b : "";
 c : "";
}

//A = A x | y;
//A = A a | A b | c;
A = x? A a | c;
//A = b A a | c | A a;
//A = (A A a | c) a*;
//A = A? A a | c;
//B = B b | B c | d;

/*D = E s1 | F s2;
E = F F;
F = D D | x;*/

/*
D = F F s1 | D D s2;
 D = D D F s1 | D D s2;
 D = x F s1 | D D s2;

D = F F s1 D'
D'= D s2 D' | E

*/
