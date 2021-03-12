token{
 x : "";
 y : "";
 a : "";
 b : "";
 c : "";
 t: "";
 r="";
}
//direct
//A = A x | y;
//A = A a | A b | c;
//A = x A a;//0=x A | c a 1=A a
A: (x? y?) A;
//A = A? A a | c;

//indirect
//A = a? c | b* (c? A)+;
//A:



