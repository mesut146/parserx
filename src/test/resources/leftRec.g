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



