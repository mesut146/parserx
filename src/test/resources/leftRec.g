A = A a | b;
B = B b | B c | d;

D = E s1 | F s2;
E = F F;
F = D D | x;
/*
D = D D D D s1 | D D | x;
D = x D';
D' = | D D D s1 D' | D D';

*/


//B = d B'?
//B' = b B' | c B'
