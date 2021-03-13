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
A = A x | y;
//A = A a | A b | c;
//A = x A a;//0=x A | c a 1=A a
//A: (x? y? a?) A;
//A = A? A a | c;

//indirect
//A = a? c | b* (c? A)+;   //1=A (c? A)* 0 = a? c | b b* (c? A)+ | b* c (c? A)*
//A: b* (c? A)+; //b b* (c? A)+ | b* (c A) (c? A)*
//A: (c? A)+; //(c A) (c? A)*



