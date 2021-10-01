token{
  a: "a";
  b: "b";
  c: "c";
  d: "d";
}

%start: E;


E: A c | a b d;
A: A B | B;
//A: B A | B;
B: a b;

/*
E: B+ c | a b d;
*/



