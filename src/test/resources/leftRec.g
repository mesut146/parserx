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
//A: b? A x | y; // b A x | A x | y===(b A x | y) x*
//A: b* A x | y; //same
A = A a | A b | c; // A=(A b | c) a*, A b a* | c a* == c a* (b a*)*
//A = x A a;//0=x A | c a 1=A a
//A: (x? y? t?) A a | b;
//A = A? A a | c;

//indirect
//A = a? c | b* (c? A)+;   //(b b* (c? A)+ | b* c A (c? A)*) | a? c
//A: b* (c? A); //b b* (c? A) | b* (c A)
//A: (c? A); //(c A) (c? A)*



