token{
  a: "a";
  b: "b";
  c: "c";
  d: "d";
}

%start: E;

/*E: A c | a b d;
A: A B | B;
B: a b;
*/

/*E: A c | a b;
A: A a | a;*/

/*E: A c | B b;
A: A a | a;
B: B a | a;*/
E: a+ (c | b);
