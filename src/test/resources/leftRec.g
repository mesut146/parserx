token{
 x : "";
 y : "";
 a : "";
 b : "";
 c : "";
 t: "";r="";
}
//direct
//A = A x | y;
//A = A a | A b | c;
//A = x? A a | c;
//A = A? A a | c;

//indirect
A = P t | r;
P = a? c | b* (c? A)+;
/*
P = a? c;
P = b* (c? A)+;
P =
*/



